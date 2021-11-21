package io.logtemplate

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import io.logtemplate.`export`.column.clickhouse.ClickhouseConfig
import io.logtemplate.`export`.index.elasticsearch.ElasticSearchConfig
import io.logtemplate.`import`.common.ReadConfig
import io.logtemplate.`import`.file.LogFileReader
import cats.effect.IO.timer
import io.logtemplate.`export`.column.clickhouse.{ClickhouseConfig, ClickhouseConnector}
import io.logtemplate.`export`.index.elasticsearch.{ElasticSearchConfig, ElasticSearchConnector}
import io.logtemplate.state.DrainConfig

import scala.concurrent.ExecutionContext

object Main {

  implicit val cs = IO.contextShift(ExecutionContext.global)
  implicit val t = timer(ExecutionContext.global)

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.parseResources("application.conf")
    val readConfig = ReadConfig(config)
    val clickhouseConfig = new ClickhouseConfig(config)
    val elasticConfig = new ElasticSearchConfig(config)
    val clickhouseConnector = new ClickhouseConnector(clickhouseConfig)

    val indexConnector = new ElasticSearchConnector(elasticConfig)
    val drainConfig = new DrainConfig(config)
    val fileReader = new LogFileReader(readConfig)
    val pipeline = new Pipeline(drainConfig, fileReader, indexConnector, clickhouseConnector)
    pipeline.work().unsafeRunSync()
  }
}
