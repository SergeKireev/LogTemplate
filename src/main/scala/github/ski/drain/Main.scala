package github.ski.drain

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import github.ski.drain.`export`.column.clickhouse.{ClickhouseConfig, ClickhouseConnector}
import github.ski.drain.`export`.index.elasticsearch.{ElasticSearchConfig, ElasticSearchConnector}
import github.ski.drain.`import`.common.{ReadConfig}
import github.ski.drain.`import`.file.LogFileReader
import github.ski.drain.state.DrainConfig

import scala.concurrent.ExecutionContext

object Main {

  implicit val cs = IO.contextShift(ExecutionContext.global)

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.parseResources("application.conf")
    val readConfig = ReadConfig(config)
    val clickhouseConfig = new ClickhouseConfig(config)
    val elasticConfig = new ElasticSearchConfig(config)
    val clickhouseConnector = new ClickhouseConnector(clickhouseConfig)

    val indexConnector = new ElasticSearchConnector(elasticConfig)
    val drainConfig = new DrainConfig(config)
    val fileReader = new LogFileReader(readConfig)
    val pipeline = new Pipeline(fileReader, drainConfig, indexConnector, clickhouseConnector)
    pipeline.work().unsafeRunSync()
  }
}
