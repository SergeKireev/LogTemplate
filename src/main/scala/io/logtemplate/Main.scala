package io.logtemplate

import cats.effect.IO
import cats.effect.std.Queue
import com.typesafe.config.ConfigFactory
import io.logtemplate.`import`.common.{FileImportConfig, ImportConfig, OpenTelemetryConfig}
import io.logtemplate.`import`.file.{FileIngestionStream, LogFileReader}
import cats.effect.unsafe.implicits.global
import io.logtemplate.`export`.column.clickhouse.{ClickhouseConfig, ClickhouseConnector}
import io.logtemplate.`export`.index.elasticsearch.{ElasticSearchConfig, ElasticSearchConnector}
import io.logtemplate.`import`.opentelemetry.service.OTReceiverService
import io.logtemplate.domain.log.LogEntry
import io.logtemplate.state.DrainConfig

object Main {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.parseResources("application.conf")

    //Importers
    val readConfig = ImportConfig(config)
    val ingestionStreamIO = readConfig match {
      case fileConfig: FileImportConfig =>
        val fileReader = new LogFileReader(fileConfig)
        IO.delay {
          FileIngestionStream.buildLogStream(fileReader)
        }
      case otConfig: OpenTelemetryConfig =>
        for {
          ingestionQueue <- Queue.unbounded[IO, Option[LogEntry]]
          openTelemetryStream <- OTReceiverService.bindStream(otConfig, ingestionQueue)
        } yield openTelemetryStream
    }

    //Exporters
    val clickhouseConfig = new ClickhouseConfig(config)
    val elasticConfig = new ElasticSearchConfig(config)
    val clickhouseConnector = new ClickhouseConnector(clickhouseConfig)
    val indexConnector = new ElasticSearchConnector(elasticConfig)

    //Pipeline
    val drainConfig = new DrainConfig(config)
    (for {
      ingestionStream <- ingestionStreamIO
      pipeline = new Pipeline(drainConfig, ingestionStream, indexConnector, clickhouseConnector)
      _ <- pipeline.work()
    } yield ()).unsafeRunSync()
  }
}
