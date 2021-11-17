package github.ski.drain.`export`.index.elasticsearch

import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}
import github.ski.drain.domain.template.Template
import github.ski.drain.token.{FreeToken, VariableToken}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.time.{Millis, Span}

import java.util.UUID
import scala.util.Random

class ElasticExportTest extends AnyFunSuite with ScalaFutures {
  def uuidGen(random: Random) = {
    new UUID(random.nextLong(), random.nextLong())
  }

  override implicit val patienceConfig = PatienceConfig(scaled(Span(2000, Millis)))

  ignore("insert a template in elasticsearch") {
    val props = ElasticProperties("http://172.17.0.2:9200")
    val client = ElasticClient(JavaClient(props))

    val elasticSearchConnector = new ElasticSearchConnector(client)

    val random = new Random(0L)
    val template1 = Template(id = uuidGen(random), tokens = List(FreeToken("User"), VariableToken(uuidGen(random)), FreeToken("has"), VariableToken(uuidGen(random))))
    elasticSearchConnector.init().futureValue
    elasticSearchConnector.insert(template1).futureValue
    val template = elasticSearchConnector.fetchDocument(template1.id).futureValue
    assert(template.nonEmpty)
    assert(template.get.id.toString === template1.id.toString)
  }
}
