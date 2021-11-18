package github.ski.drain.`export`.column.clickhouse

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import cats.effect.{ContextShift, IO}
import com.crobox.clickhouse.ClickhouseClient
import com.crobox.clickhouse.internal.QuerySettings
import com.crobox.clickhouse.stream.{ClickhouseSink, Insert}
import com.typesafe.config.ConfigFactory
import github.ski.drain.`export`.column.{ColumnConnector, VariableRecord}
import github.ski.drain.domain.template.{VDouble, VLong, VString, VariableType}
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, JsonObject}

import java.text.SimpleDateFormat
import java.util.UUID
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import cats.implicits._

object codec {
  private def mapVTypeToClickhouseCol(vType: VariableType) = vType match {
    case VString => "string"
    case VLong => "long"
    case VDouble => "double"
  }

  lazy implicit val encoder: Encoder[VariableRecord] = Encoder.instance[VariableRecord] {
    case variableRecord: VariableRecord =>
      val format = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss")
      val dateTime = format.format(variableRecord.date)
      val obj = JsonObject.empty
        .add("id", variableRecord.template.id.asJson)
        .add("ts", dateTime.asJson)
      val variableCounts = mutable.Map.empty[VariableType, Int]
      variableRecord.variables.foldLeft(obj) {
        case (objAcc, v) =>
          variableCounts.updateWith(v.`type`) {
            case None => Some(1)
            case Some(c) => Some(c+1)
          }
          val index = variableCounts(v.`type`)
          val clickhouseCol = mapVTypeToClickhouseCol(v.`type`)
          objAcc.add(s"${clickhouseCol}_name_${index}", v.name.asJson)
              .add(s"${clickhouseCol}_val_${index}", v.`type`.toJson(v.value))
             .add(s"${clickhouseCol}_id_${index}", v.id.asJson)
      }.asJson
  }
}

import codec._
class ClickhouseConnector(config: ClickhouseConfig) extends ColumnConnector[IO] {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  lazy val client = {
    val croboxConfig = config.adaptToCrobox()
    new ClickhouseClient(Some(croboxConfig))
  }

  val DB_NAME = "template"
  val TABLE_NAME = "variables"

  private lazy val schema =
    scala.io.Source
      .fromFile("src/main/resources/export/clickhouse/schema.sql")
      .mkString

  override def insert(variableRecords: List[VariableRecord]): IO[Unit] = {
    implicit val querySettings = new QuerySettings()
    IO.fromFuture(
      IO.delay(
        client.execute(s"INSERT INTO $DB_NAME.$TABLE_NAME FORMAT JSONEachRow", variableRecords.map(_.asJson.noSpaces).mkString("\n")).void
      )
    )
  }

  override def getForTemplateId(id: UUID): IO[List[Any]] = ???

  override def init(): IO[Unit] = {
    IO.fromFuture(
      IO.delay(
        for {
          _ <- client.execute(s"CREATE DATABASE IF NOT EXISTS $DB_NAME")
          _ <- client.execute(schema)
        } yield ()
      )
    )
  }
}
