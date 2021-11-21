package io.logtemplate.`import`

import io.logtemplate.domain.log.LogEntry

trait AbstractReader[F[_]] {
  def open(): F[Unit]
  def read(): F[Option[LogEntry]]
  def close(): F[Unit]
}
