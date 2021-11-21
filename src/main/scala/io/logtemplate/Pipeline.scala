package io.logtemplate

import cats.effect.{ContextShift, Sync}
import com.typesafe.scalalogging.LazyLogging
import io.logtemplate.`export`.column.ColumnConnector
import io.logtemplate.`import`.AbstractReader
import io.logtemplate.state.DrainStateController
import fs2.Stream
import cats.implicits._
import io.logtemplate.`export`.column.{ColumnConnector, VariableRecord}
import io.logtemplate.`export`.index.IndexConnector
import io.logtemplate.domain.log.LogEntry
import io.logtemplate.state.{DrainConfig, DrainState, DrainStateController}

class Pipeline[F[_]](drainConfig: DrainConfig,
               reader: AbstractReader[F],
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
    S.delay(new Drain(DrainState(), drainConfig))
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
      _ <- columnConnector.init()
      drain <- buildDrain()
      _ <- buildLogStream.map {
        logEntry =>
          val tokens = drain.preprocess(logEntry.content)
          val (template, structuredTokens) = drain.process(tokens)
          val variables = drain.postProcess(template, structuredTokens)
          val variableRecord = VariableRecord(logEntry.date, template, variables)
          variableRecord
      }.chunkN(drainConfig.exportBatchSize, true)
      .evalMap {
        variableRecords =>
          for {
            _ <- columnConnector.insertVariables(variableRecords.toList)
            _ <- columnConnector.insertTemplates(variableRecords.map(_.template).toList.distinctBy(_.id))
          } yield {
            logger.info(s"Inserted ${variableRecords.size}")
          }
      }.compile.drain
    } yield {
      logger.info("Finished processing file")
    }
  }
}
