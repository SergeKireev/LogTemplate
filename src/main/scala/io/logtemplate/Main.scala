package io.logtemplate

import cats.implicits._
import cats.effect.{IO, Resource}
import cats.effect.std.Queue
import com.typesafe.config.ConfigFactory
import io.logtemplate.`import`.common.{FileImportConfig, ImportConfig, OpenTelemetryConfig}
import io.logtemplate.`import`.file.{FileIngestionStream, LogFileReader}
import cats.effect.unsafe.implicits.global
import fs2.Stream
import io.logtemplate.`export`.column.clickhouse.{ClickhouseConfig, ClickhouseConnector}
import io.logtemplate.`export`.index.elasticsearch.{ElasticSearchConfig, ElasticSearchConnector}
import io.logtemplate.`import`.opentelemetry.service.OTReceiverService
import io.logtemplate.domain.log.LogEntry
import io.logtemplate.state.DrainConfig

import java.io.File

object Main {
  private def dequeue[F[_], A](queue: Queue[F, Option[A]]): Stream[F, A] = {
    Stream.repeatEval(queue.take).unNoneTerminate
  }

  private def ingestionStreamProvider(readConfig: ImportConfig, queue: Queue[IO, Option[LogEntry]]): IO[Stream[IO, LogEntry]] = {
    readConfig match {
      case fileConfig: FileImportConfig =>
        val fileReader = new LogFileReader(fileConfig)
        IO.delay {
          FileIngestionStream.buildLogStream(fileReader)
        }
      case _: OpenTelemetryConfig =>
        IO.delay {
          dequeue(queue)
        }
    }
  }

  private def ingestionServiceBind(readConfig: ImportConfig, queue: Queue[IO, Option[LogEntry]]): IO[Nothing] = {
    readConfig match {
      case _: FileImportConfig =>
        val resource = Resource.eval(IO.delay())
        resource.useForever
      case otConfig: OpenTelemetryConfig =>
        OTReceiverService.bindStream(otConfig, queue)
    }
  }

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.parseFile(new File("./src/main/resources/application.conf"))

    //Importers
    val readConfig = ImportConfig(config)

    //Exporters
    val clickhouseConfig = new ClickhouseConfig(config)
    val elasticConfig = new ElasticSearchConfig(config)
    val clickhouseConnector = new ClickhouseConnector(clickhouseConfig)
    val indexConnector = new ElasticSearchConnector(elasticConfig)

    //Pipeline
    val drainConfig = new DrainConfig(config)
    (for {
      queue <- Queue.unbounded[IO, Option[LogEntry]]
      ingestionStream <- ingestionStreamProvider(readConfig, queue)
      pipeline = new Pipeline(drainConfig, ingestionStream, indexConnector, clickhouseConnector)
      _ <- ((pipeline.work(), ingestionServiceBind(readConfig, queue))).parBisequence
    } yield ()).unsafeRunSync()
  }
}
