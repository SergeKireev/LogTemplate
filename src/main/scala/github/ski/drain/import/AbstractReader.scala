package github.ski.drain.`import`

import github.ski.drain.domain.log.LogEntry

trait AbstractReader[F[_]] {
  def open(): F[Unit]
  def read(): F[Option[LogEntry]]
  def close(): F[Unit]
}
