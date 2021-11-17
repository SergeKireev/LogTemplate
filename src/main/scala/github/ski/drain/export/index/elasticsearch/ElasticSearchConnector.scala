package github.ski.drain.`export`.index.elasticsearch

import com.sksamuel.elastic4s.{ElasticClient, RequestFailure, RequestSuccess}
import github.ski.drain.`export`.index.IndexConnector
import github.ski.drain.domain.template.Template
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.fields.TextField
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.requests.searches.queries.SimpleStringQuery
import github.ski.drain.state.serialize.DrainStateCodec._
import io.circe.parser
import io.circe.syntax._

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ElasticSearchConnector(client: ElasticClient) extends IndexConnector[Future] {

  def init(): Future[Unit] = {
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

  override def insert(template: Template): Future[Unit] = {
    client.execute(
      indexInto("templates")
        .fields(
          "template_id" -> template.id,
          "printed_template" -> template.print(),
          "serialized_template" -> template.asJson.noSpaces)
        .refresh(RefreshPolicy.Immediate)
    ).map{
      _ => ()
    }
  }

  /**
   * Fetches a document by id
   *
   * @param id
   * @return
   */
  override def fetchDocument(id: UUID): Future[Option[Template]] = {
    (client.execute {
      search("templates")
        .query(SimpleStringQuery(id.toString)
          .field("template_id"))
    }).flatMap {
      case failure: RequestFailure => Future.failed(new Exception(failure.error.reason))
      case results: RequestSuccess[SearchResponse] =>
        Future.successful(for {
          hit <- results.result.hits.hits.headOption
          field <- hit.sourceAsMap.get("serialized_template")
          json <- parser.parse(field.toString).toOption
          template <- json.as[Template].toOption
        } yield template)
      case _: RequestSuccess[_] => Future.successful(None)
    }
  }
}
