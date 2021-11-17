package github.ski.drain.util

import com.typesafe.config.Config

import scala.util.Try

object ConfigOps {
  implicit class WithFallback(config: Config) {
    def getOrElse[T](path: String, fallback: T) = Try(config.getAnyRef(path).asInstanceOf[T]).getOrElse(fallback)
  }
}
