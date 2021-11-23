package io.logtemplate.`import`.file

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import io.logtemplate.`import`.AbstractReader
import io.logtemplate.`import`.common.{Ingestion, ReadConfig}
import io.logtemplate.domain.log.LogEntry

import java.io.{BufferedReader, FileReader}
import scala.collection.mutable.ListBuffer
import scala.util.Failure

class LogFileReader(config: ReadConfig) extends AbstractReader[IO] with LazyLogging {
  private var br: BufferedReader = null
  private var currentLine: String = null

  lazy val ingestion: Ingestion = new Ingestion(config)

  def open() = {
    IO.delay {
      br = new BufferedReader(new FileReader(config.filePath))
      currentLine = br.readLine
    }
  }

  def read(): IO[Option[LogEntry]] = {
    IO.delay {
      Option(currentLine).flatMap {
        _ =>
          var nextLine: String = null
          val buffer: ListBuffer[String] = new ListBuffer[String]()
          buffer += currentLine
          var count = 0
          while ({nextLine = br.readLine(); nextLine != null &&
            ingestion.tryParseLine(nextLine).isFailure &&
            count < config.multiLineLimit}) {
              buffer += nextLine
              count = count + 1
          }
          val logEntryStr = buffer.mkString("\n")
          currentLine = nextLine
          ingestion.tryParseLine(logEntryStr).recoverWith {
            case e: Exception =>
              logger.error("Parsing of some lines of the file failed", e)
              Failure(e)
          }.toOption
      }
    }
  }

  def nbOfLines = {
    IO.delay {
      scala.io.Source.fromFile(config.filePath).getLines.size
    }
  }

  def close() = {
    IO.delay(br.close)
  }
}
