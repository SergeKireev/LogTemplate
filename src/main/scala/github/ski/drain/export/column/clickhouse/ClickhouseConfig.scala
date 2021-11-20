package github.ski.drain.`export`.column.clickhouse

import com.typesafe.config.{Config, ConfigFactory}
import github.ski.drain.util.ConfigOps.WithFallback

class ClickhouseConfig(config: Config) {
  def scoped: Config = config.getConfig("export.clickhouse")

  def getHost() = scoped.getOrElse[String]("host", "localhost")

  def adaptToCrobox(): Config = {
    ConfigFactory.parseString(
    s"""
      |crobox.clickhouse.client {
      |    connection: {
      |        type = "single-host",
      |        host = ${getHost()},
      |        port = 8123
      |    },
      |    maximum-frame-length: 100000,
      |    retries: 2,
      |    custom: {},
      |    settings: {
      |      custom: {}
      |    },
      |    buffer-size: 8192
      |}
      |""".stripMargin)
  }
}
