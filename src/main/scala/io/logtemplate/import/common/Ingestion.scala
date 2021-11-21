package io.logtemplate.`import`.common

import io.logtemplate.domain.log.LogEntry

import scala.util.Try

class Ingestion(config: ReadConfig) {
  val dissect = new Dissect(config.pattern, config.dateFormat)

  def tryParseLine(s: String): Try[LogEntry] = {
    dissect.extractLogEvent(s)
  }
}
