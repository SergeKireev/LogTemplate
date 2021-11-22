package io.logtemplate.`import`.common

import com.typesafe.config.{Config, ConfigFactory}
import io.logtemplate.util.ConfigOps.WithFallback

sealed trait Pattern
case class DissectPattern(pattern: String)

case class ReadConfig(config: Config) {
  private def scoped() = config.getConfig("read")
  def fileName: String = scoped.getOrElse("file.path", "src/resources/file.log")
  def pattern: DissectPattern = DissectPattern(scoped.getOrElse("dissect.pattern", "%{ts} %{+ts} %{msg}"))
  def dateFormat: String = scoped.getOrElse("date-format", "yyyy-MM-dd hh:mm:ss")
}
