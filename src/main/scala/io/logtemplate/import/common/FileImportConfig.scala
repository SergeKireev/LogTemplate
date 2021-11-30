package io.logtemplate.`import`.common

import com.typesafe.config.{Config, ConfigFactory}
import io.logtemplate.util.ConfigOps.WithFallback

import scala.util.Try

sealed trait Pattern
case class DissectPattern(pattern: String)


object ImportConfig {
  def apply(config: Config): ImportConfig = {
      val importConfig = config.getConfig("import")
      val fileAttempt = Try(importConfig.getConfig("file"))
      val openTelemetryAttempt = Try(importConfig.getConfig("opentelemetry"))
      (fileAttempt.map(_ => FileImportConfig(importConfig)).toOption ++
        openTelemetryAttempt.map(_ => OpenTelemetryConfig(importConfig)).toOption).head
  }
}

sealed trait ImportConfig {
  val config: Config
}

case class FileImportConfig(config: Config) extends ImportConfig {
  private def scoped() = config.getConfig("file")
  def filePath: String = scoped.getOrElse("path", "src/resources/file.log")
  def pattern: DissectPattern = DissectPattern(scoped.getOrElse("dissect.pattern", "%{ts} %{+ts} %{msg}"))
  def dateFormat: String = scoped.getOrElse("date-format", "yyyy-MM-dd hh:mm:ss")
  def multiLineLimit: Int = scoped.getOrElse("multiline.limit", 1000)
}

case class OpenTelemetryConfig(config: Config) extends ImportConfig {
  private def scoped() = config.getConfig("opentelemetry")
  def port(): Int = scoped().getOrElse("port", 9999)
}