package github.ski.drain

import cats.effect.{Bracket, ContextShift, Sync}
import com.typesafe.scalalogging.LazyLogging
import github.ski.drain.`export`.column.{ColumnConnector, VariableRecord}
import github.ski.drain.`export`.index.IndexConnector
import github.ski.drain.`import`.AbstractReader
import github.ski.drain.state.{DrainConfig, DrainState, DrainStateController}
import fs2.Stream

import scala.concurrent.duration.Duration
import cats.implicits._
import github.ski.drain.domain.log.LogEntry

class Pipeline[F[_]](reader: AbstractReader[F],
               drainConfig: DrainConfig,
               indexConnector: IndexConnector[F],
               columnConnector: ColumnConnector[F])
                    (implicit val S: Sync[F],
                                  CS: ContextShift[F]) extends LazyLogging {

  type StreamF[A] = Stream[F, A]

  def loadState(): F[DrainState] = {
    indexConnector.fetchTemplates().map {
      templates =>
        logger.info(s"Found ${templates.size} templates in elasticseach")
        val state = DrainState()
        val stateController = new DrainStateController(state, drainConfig)
        templates.foreach {
          t =>
            logger.info(s"Loading ${t.print()}")
            stateController.insertTemplate(t)
        }
        stateController.getState()
    }
  }

  def buildDrain(): F[Drain] = {
    loadState().map {
      state =>
        new Drain(state, drainConfig)
    }
  }

  def buildLogStream(): Stream[F, LogEntry] = {
    Stream.bracket(reader.open())(_ => reader.close()).flatMap {
      _ =>
        Stream.repeatEval(reader.read()).unNoneTerminate
    }
  }

  def work(): F[Unit] = {
    logger.info("Opening reader")
    reader.open()

    for {
      _ <- indexConnector.init()
      _ <- columnConnector.init()
      drain <- buildDrain()
      _ <- buildLogStream.map {
        logEntry =>
          val tokens = drain.preprocess(logEntry.content)
          val (template, structuredTokens) = drain.process(tokens)
          val variables = drain.postProcess(template, structuredTokens)
          val variableRecord = VariableRecord(logEntry.date, template, variables)
          variableRecord
      }.chunkN(1000, true)
      .evalMap {
        variableRecords =>
          for {
            _ <- columnConnector.insert(variableRecords.toList)
            _ <- indexConnector.insertBulk(variableRecords.map(_.template).toList)
          } yield {
            logger.info(s"Inserted ${variableRecords.size}")
          }
      }.compile.drain
    } yield ()
  }
}
