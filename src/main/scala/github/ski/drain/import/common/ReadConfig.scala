package github.ski.drain.`import`.common

import com.typesafe.config.Config
import github.ski.drain.util.ConfigOps.WithFallback

case class Multiline(config: Config) {
  private def scoped = config.getConfig("multiline")

  def `type`: String = scoped.getOrElse("type", "filter")
  def pattern: String = scoped.getOrElse("pattern", "")
  def negate: Boolean = scoped.getOrElse("negate", false)
  def `match`: String = scoped.getOrElse("match", "after")
}

sealed trait Pattern
case class DissectPattern(pattern: String)

case class ReadConfig(config: Config) {
  private def scoped() = config.getConfig("read")
  def fileName: String = scoped.getOrElse("file.path", "src/resources/file.log")
  def pattern: DissectPattern = DissectPattern(scoped.getOrElse("dissect.pattern", "%{ts} %{msg}"))
  def multiline: Multiline = Multiline(scoped)
}
