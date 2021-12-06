package io.logtemplate.`import`.opentelemetry.service

import cats.implicits._
import cats.effect.std.Queue
import cats.effect.{IO, Resource}
import com.typesafe.scalalogging.LazyLogging
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import fs2.grpc.syntax.all._
import io.grpc.{Metadata, ServerServiceDefinition}
import io.logtemplate.`import`.common.{Ingestion, OpenTelemetryConfig, RawImportConfig}
import io.logtemplate.domain.log.LogEntry
import io.opentelemetry.proto.collector.logs.v1.logs_service.{ExportLogsServiceRequest, ExportLogsServiceResponse, LogsServiceFs2Grpc}

class LogReceivingService(config: RawImportConfig, queue: Queue[IO, Option[LogEntry]]) extends LogsServiceFs2Grpc[IO, Metadata] with LazyLogging {
  lazy val ingestion: Ingestion = new Ingestion(config)

  override def `export`(request: ExportLogsServiceRequest, ctx: Metadata): IO[ExportLogsServiceResponse] = {
    val logs = request.resourceLogs.flatMap(_.instrumentationLibraryLogs).flatMap(_.logs)
    val response = ExportLogsServiceResponse()
    for {
      _ <- logs.flatMap {
        log =>
          ingestion.tryParseLine(log.getBody.getStringValue).recoverWith {
            case e =>
              logger.error("Could not parse log event", e)
              scala.util.Failure(e)
          }.toOption
        }
        .traverse {
          log =>
            queue.offer(
              Some(log)
            )
        }
    } yield response
  }
}

object OTReceiverService extends LazyLogging {
  def bindStream(ot: OpenTelemetryConfig, queue: Queue[IO, Option[LogEntry]]) = {
    logger.info("Binding open telemetry stream")
    val OtIngestionService: Resource[IO, ServerServiceDefinition] =
        LogsServiceFs2Grpc.bindServiceResource[IO](new LogReceivingService(ot, queue))

    def run(service: ServerServiceDefinition) = NettyServerBuilder
      .forPort(ot.port())
      .addService(service)
      .resource[IO]
      .evalMap(server => IO(server.start()))
      .useForever

    OtIngestionService.use(run)
  }

}
