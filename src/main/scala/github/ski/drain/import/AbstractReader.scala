package github.ski.drain.`import`

import github.ski.drain.domain.log.LogEntry

trait AbstractReader {
  def open(): Unit
  def read(): Option[LogEntry]
  def close(): Unit
}
