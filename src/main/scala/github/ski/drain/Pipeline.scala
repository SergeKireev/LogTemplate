package github.ski.drain

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import github.ski.drain.`export`.column.{ColumnConnector, VariableRecord}
import github.ski.drain.`export`.index.IndexConnector
import github.ski.drain.`import`.AbstractReader
import github.ski.drain.state.{DrainConfig, DrainState}
import github.ski.drain.token.{BracketAwareTokenizer, SimpleTokenizer}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class Pipeline(reader: AbstractReader,
               drainConfig: DrainConfig,
               indexConnector: IndexConnector[Future],
               columnConnector: ColumnConnector[Future]) extends LazyLogging {

  def loadState(): DrainState = {
    //TODO: Load state from elasticsearch
    DrainState()
  }

  def buildDrain() = {
    new Drain(loadState(), drainConfig)
  }

  def work(): Unit = {
    logger.info("Opening reader")
    reader.open()

    indexConnector.init()
    columnConnector.init()

    val drain = buildDrain()

    var currentLogEntry = reader.read()
    while (currentLogEntry.nonEmpty) {
      val logEntry = currentLogEntry.get
      logger.info(s"Preprocessing $logEntry")
      val tokens = drain.preprocess(logEntry.content)
      logger.info(s"Tokens ${tokens}")
      val (template, structuredTokens) = drain.process(tokens)
      val variables = drain.postProcess(template, structuredTokens)
      currentLogEntry = reader.read()
      val variableRecord = VariableRecord(logEntry.date, template, variables)

      // Insertion
      Await.result(columnConnector.insert(variableRecord), 0 nanos)
      Await.result(indexConnector.insert(template), 0 nanos)
    }
    reader.close()
  }
}
