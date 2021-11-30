package io.logtemplate

import cats.effect.{Async, Concurrent, Sync}
import com.typesafe.scalalogging.LazyLogging
import io.logtemplate.`import`.AbstractReader
import fs2.Stream
import cats.implicits._
import cats.effect.std.Queue
import io.logtemplate.`export`.column.ColumnConnector
import io.logtemplate.`export`.index.IndexConnector
import io.logtemplate.domain.log.LogEntry
import io.logtemplate.state.{DrainConfig, DrainState, DrainStateController}

object ShardedOps {

  private def dequeue[F[_], A](queue: Queue[F, Option[A]]): Stream[F, A] = {
    Stream.repeatEval(queue.take).unNoneTerminate
  }

  private def _sharded[F[_]: Concurrent, V, R](
                                          input: Stream[F, (Int, V)],
                                          process: (Int, V) => F[R],
                                          numShards: Int
                                        ): Stream[F, R] =
    Stream
      .eval(Queue.unbounded[F, Option[(Int, V)]]) // unbounded for simplicity
      .repeatN(numShards.toLong)
      .foldMap(Vector(_))
      .flatMap { shards =>
        val close = shards.traverse_(_.offer(None))

        val writer = input
          .evalMap {
            case v @ (k, _) =>
              shards(k % numShards).offer(v.some)
          }
          .onFinalize(close)

        val readers =
          Stream
            .emits(shards)
            .map(dequeue(_).evalMap(process.tupled))
            .parJoinUnbounded

        readers concurrently writer
      }

  implicit class shardedSyntax[F[_]: Concurrent, V](stream: Stream[F, (Int, V)]) {
    def sharded[R](
                 process: (Int, V) => F[R],
                 numShards: Int
               ) = {
      _sharded(stream, process, numShards)
    }
  }
}
import ShardedOps._

class Pipeline[F[_]](drainConfig: DrainConfig,
                     ingestionStream: Stream[F, LogEntry],
                     indexConnector: IndexConnector[F],
                     columnConnector: ColumnConnector[F])
                    (implicit val C: Concurrent[F], S: Async[F]) extends LazyLogging {

  type StreamF[A] = Stream[F, A]

  def loadState(): F[DrainState] = {
    indexConnector.fetchTemplates().map {
      templates =>
        logger.debug(s"Found ${templates.size} templates in elasticseach")
        val state = DrainState()
        val stateController = new DrainStateController(state, drainConfig)
        templates.foreach {
          t =>
            logger.debug(s"Loading ${t.print()}")
            stateController.insertTemplate(t)
        }
        stateController.getState()
    }
  }

  def buildDrainWorker(): F[DrainWorker] = {
    S.delay(new DrainWorker(new Drain(DrainState(), drainConfig)))
  }

  def work(): F[Unit] = {
    logger.info("Opening reader")
    val numShards = drainConfig.parallelism
    val preprocessingWorkerF = buildDrainWorker()
    val workersF = (0 to numShards).toList.traverse(i => buildDrainWorker().map(d => (i, d))).map(_.toMap)

    for {
      _ <- columnConnector.init()
      preprocessingWorker <- preprocessingWorkerF
      workers <- workersF
      _ <- ingestionStream
        .parEvalMapUnordered(drainConfig.parallelism) {
          logEntry =>
            val tokens = preprocessingWorker.preprocess(logEntry.content)
            val shard = tokens.size % numShards
            S.delay((shard, (logEntry, tokens)))
        }
        .sharded(
          (k, v) => S.delay(workers(k).work(v._1, v._2)),
          numShards
        )
        .chunkN(drainConfig.exportBatchSize, true)
        .parEvalMapUnordered(drainConfig.parallelism) {
          variableRecords =>
            (for {
              _ <- columnConnector.insertVariables(variableRecords.compact.values)
              _ <- columnConnector.insertTemplates(variableRecords.map(_.template).toList.distinctBy(_.id))
            } yield {
              variableRecords.size
            })
        }
        .scan(0) {
          case (acc, batchSize) => {
            val SAMPLE_SIZE = 10000
            if (batchSize % SAMPLE_SIZE == 0)
              logger.info(s"Treated estimated $acc lines")
            acc+batchSize
          }
      }.compile.drain
    } yield {
      logger.info("Finished processing file")
    }
  }
}
