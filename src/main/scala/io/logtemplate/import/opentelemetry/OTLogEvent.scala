package io.logtemplate.`import`.opentelemetry

import io.opentelemetry.proto.logs.v1.logs.LogRecord

/*
 * As described here https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/logs/data-model.md#type-any:
 */

object OTLogEvent {
  def fromProto(log: LogRecord): OTLogEvent = ???

  def toProto(): LogRecord = ???
}

case class OTLogEvent(
  timestamp: Long, //	Time when the event occurred.
  traceId: String, //	Request trace id.
  spanId: String, //	Request span id.
  traceFlags: Short, //	W3C trace flag.
  severityText: String, //	The severity text (also known as log level).
  severityNumber: Short, //	Numerical value of the severity.
  name: String, //	Short event identifier.
  body: Any, //	The body of the log record.
  resource: String, //	Describes the source of the log.
  attributes: Map[String, Any] //	Additional information about the event.
) {
}
