package io.logtemplate.domain.log

import java.util.Date

case class LogEntry(date: Date, metaData: Map[String, String], content: String)
