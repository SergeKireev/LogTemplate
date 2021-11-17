package github.ski.drain.`import`.file

import github.ski.drain.`import`.AbstractReader
import github.ski.drain.`import`.common.{Ingestion, ReadConfig}
import github.ski.drain.domain.log.LogEntry

import java.io.{BufferedReader, FileReader}
import scala.collection.mutable.ListBuffer

class LogFileReader(config: ReadConfig) extends AbstractReader {
  private var br: BufferedReader = null
  private var currentLine: String = null

  lazy val ingestion: Ingestion = new Ingestion(config)

  def open() = {
    br = new BufferedReader(new FileReader(config.fileName))
    currentLine = br.readLine
  }

  def read(): Option[LogEntry] = {
    Option(currentLine).flatMap {
      _ =>
        var nextLine: String = null
        val buffer: ListBuffer[String] = new ListBuffer[String]()
        buffer += currentLine
        while ({nextLine = br.readLine(); nextLine != null && ingestion.tryParseLine(nextLine).isFailure}) {
          buffer += nextLine
        }
        val logEntryStr = buffer.mkString("\n")
        currentLine = nextLine
        ingestion.tryParseLine(logEntryStr).toOption
    }
  }

  def close() = {
    br.close
  }
}
