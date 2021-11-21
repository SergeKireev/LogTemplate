package io.logtemplate.`export`.index.elasticsearch

import cats.effect.{ContextShift, IO, Timer}
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.fields.{DateField, TextField}
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import io.logtemplate.state.serialize.DrainStateCodec._
import io.circe.parser
import io.circe.syntax._

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import fs2.Stream
import io.logtemplate.`export`.index.IndexConnector
import io.logtemplate.domain.template.Template

import scala.language.postfixOps
import scala.concurrent.duration.DurationInt

class ElasticSearchConnector(config: ElasticSearchConfig)(implicit cs: ContextShift[IO], timer: Timer[IO]) extends IndexConnector[IO] {

  lazy val client: IO[ElasticClient] = {
    val props = ElasticProperties(s"http://${config.getHost()}:9200")
    Stream.retry(
      IO.delay {
        init()
      }, 5 seconds, d => d, 10)
      .compile
      .lastOrError
      .map {
      _ =>
        ElasticClient(JavaClient(props))
    }
  }

  def init(): IO[Unit] = {
    client.flatMap {
      c =>
        IO.fromFuture {
          IO.delay {
            c.execute {
              createIndex("templates").mapping(
                properties(
                  TextField("template_id"),
                  TextField("printed_template"),
                  TextField("serialized_template"),
                  DateField("ts")
                )
              )
            }.map {
              _ => ()
            }
          }
        }
    }
  }

  override def insert(template: Template): IO[Unit] = {
    client.flatMap {
      c =>
        IO.fromFuture {
          IO.delay {
            c.execute(
              indexInto("templates").id(template.id.toString)
                .fields(
                  "printed_template" -> template.print(),
                  "serialized_template" -> template.asJson.noSpaces)
                .refresh(RefreshPolicy.Immediate)
            ).map {
              _ => ()
            }
          }
        }
    }
  }

  override def insertBulk(templates: List[Template]): IO[Unit] = {
    val operations = templates.distinctBy(t => t.id).map {
      template =>
        indexInto("templates").id(template.id.toString)
          .fields(
            "printed_template" -> template.print(),
            "serialized_template" -> template.asJson.noSpaces)
    }
    client.flatMap {
      c =>
        IO.fromFuture {
          IO.delay {
            c.execute(
              bulk(operations).refresh(RefreshPolicy.Immediate)
            ).map {
              _ => ()
            }
          }
        }
    }
  }

  private def templatesFromHits(results: RequestSuccess[SearchResponse]): List[Template] = {
    (for {
      hit <- results.result.hits.hits
      field <- hit.sourceAsMap.get("serialized_template").toArray
      json <- parser.parse(field.toString).toOption.toArray
      template <- json.as[Template].toOption.toArray
    } yield template).toList
  }

  /**
   * Fetches a document by id
   *
   * @param id
   * @return
   */
  override def fetchTemplate(id: UUID): IO[Option[Template]] = {
    client.flatMap {
      c =>
        IO.fromFuture {
          IO.delay {
            c.execute {
              get("templates", id.toString)
            }.flatMap {
              case failure: RequestFailure => Future.failed(new Exception(failure.error.reason))
              case results: RequestSuccess[SearchResponse] =>
                Future.successful(templatesFromHits(results).headOption)
              case _: RequestSuccess[_] => Future.successful(None)
            }
          }
        }
    }
  }

  override def fetchTemplates(): IO[List[Template]] = {
    client.flatMap {
      c =>
        IO.fromFuture {
          IO.delay {
            //TODO: Implement scroll search over all templates
            c.execute {
              search("templates").limit(1000)
            }.flatMap {
              case failure: RequestFailure => Future.failed(new Exception(failure.error.reason))
              case results: RequestSuccess[SearchResponse] =>
                Future.successful(templatesFromHits(results))
              case _: RequestSuccess[_] => Future.successful(Nil)
            }
          }
        }
    }
  }
}
