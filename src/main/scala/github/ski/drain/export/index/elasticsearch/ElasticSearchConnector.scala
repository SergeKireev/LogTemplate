package github.ski.drain.`export`.index.elasticsearch

import cats.effect.{ContextShift, IO}
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties, RequestFailure, RequestSuccess}
import github.ski.drain.`export`.index.IndexConnector
import github.ski.drain.domain.template.Template
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.fields.TextField
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import github.ski.drain.state.serialize.DrainStateCodec._
import io.circe.parser
import io.circe.syntax._

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ElasticSearchConnector(config: ElasticSearchConfig)(implicit cs: ContextShift[IO]) extends IndexConnector[IO] {

  lazy val client = {
    val props = ElasticProperties(s"http://${config.getHost()}:9200")
    ElasticClient(JavaClient(props))
  }

  def init(): IO[Unit] = {
    IO.fromFuture {
      IO.delay {
        client.execute {
          createIndex("templates").mapping(
            properties(
              TextField("template_id"),
              TextField("printed_template"),
              TextField("serialized_template")
            )
          )
        }.map{
          _ => ()
        }
      }
    }
  }

  override def insert(template: Template): IO[Unit] = {
    IO.fromFuture {
      IO.delay {
        client.execute(
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

  override def insertBulk(templates: List[Template]): IO[Unit] = {
    val operations = templates.distinctBy(t => t.id).map {
      template =>
        indexInto("templates").id(template.id.toString)
          .fields(
            "printed_template" -> template.print(),
            "serialized_template" -> template.asJson.noSpaces)
    }
    IO.fromFuture {
      IO.delay {
        client.execute(
          bulk(operations).refresh(RefreshPolicy.Immediate)
        ).map {
          _ => ()
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
    IO.fromFuture {
      IO.delay {
        client.execute {
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

  override def fetchTemplates(): IO[List[Template]] = {
    IO.fromFuture {
      IO.delay {
        //TODO: Implement scroll search over all templates
        client.execute {
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
