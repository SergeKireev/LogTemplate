package io.logtemplate.`import`.opentelemetry.service

import cats.implicits._
import cats.effect.std.Queue
import cats.effect.{IO, Resource}
import com.typesafe.scalalogging.LazyLogging
import fs2.Stream
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import fs2.grpc.syntax.all._
import io.grpc.{Metadata, ServerServiceDefinition}
import io.logtemplate.`import`.common.OpenTelemetryConfig
import io.logtemplate.domain.log.LogEntry
import io.opentelemetry.proto.collector.logs.v1.logs_service.{ExportLogsServiceRequest, ExportLogsServiceResponse, LogsServiceFs2Grpc}

import java.util.Date

class LogReceivingService(queue: Queue[IO, Option[LogEntry]]) extends LogsServiceFs2Grpc[IO, Metadata] with LazyLogging {
  override def `export`(request: ExportLogsServiceRequest, ctx: Metadata): IO[ExportLogsServiceResponse] = {
    val logs = request.resourceLogs
    val response = ExportLogsServiceResponse()
    println(s"RECEIVED LOGS FROM EXPORTER ${logs.mkString("\n")}")
    for {
      _ <- logs.traverse {
        l =>
          queue.offer(
            Some(LogEntry(new Date, Map.empty, ""))
          )
      }
    } yield response
  }
}

object OTReceiverService {
  private def dequeue[F[_], A](queue: Queue[F, Option[A]]): Stream[F, A] = {
    Stream.repeatEval(queue.take).unNoneTerminate
  }

  def bindStream(ot: OpenTelemetryConfig, queue: Queue[IO, Option[LogEntry]]) = {
    val OtIngestionService: Resource[IO, ServerServiceDefinition] =
        LogsServiceFs2Grpc.bindServiceResource[IO](new LogReceivingService(queue))

    def run(service: ServerServiceDefinition) = NettyServerBuilder
      .forPort(ot.port())
      .addService(service)
      .resource[IO]
      .evalMap(server => IO(server.start()))
      .useForever

    OtIngestionService.use(run).map {
      _: Nothing =>
        dequeue(queue)
    }
  }

}
