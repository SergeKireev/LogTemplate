package github.ski.drain.`export`.column.clickhouse

import com.crobox.clickhouse.ClickhouseClient
import com.typesafe.config.ConfigFactory
import github.ski.drain.`export`.column.VariableRecord
import github.ski.drain.domain.template.{Template, VLong, VString, Variable}
import github.ski.drain.token.{FreeToken, VariableToken}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.time.{Millis, Span}

import java.util.{Date, UUID}
import scala.util.Random

class ClickhouseConnectorTest extends AnyFunSuite with ScalaFutures {

  override implicit val patienceConfig = PatienceConfig(scaled(Span(10000, Millis)))

  def uuidGen(random: Random) = {
    new UUID(random.nextLong(), random.nextLong())
  }

  ignore("export variables to clickhouse") {
    val config =
      ConfigFactory.parseString("""
        |clickhouse {
        |  host = "172.17.0.2"
        |}
        |""".stripMargin)
    val clickhouseConnector = new ClickhouseConnector(new ClickhouseConfig(config))
    val result = clickhouseConnector.init().futureValue
    val random = new Random(0L)
    val template1 = Template(id = uuidGen(random), tokens = List(FreeToken("User"), VariableToken(uuidGen(random)), FreeToken("has"), VariableToken(uuidGen(random))))
    val variables = List(
        Variable(UUID.fromString("2ea4f76e-fed3-4489-9173-bd03b6b7e0bc"),"int2ea4f76e",VLong,"678"),
        Variable(UUID.fromString("2ea4f77e-fed3-4489-9173-bd03b6b7e0bc"),"string2ea4f77e",VString,"connected"),
        Variable(UUID.fromString("2ea4f79e-fed3-4489-9173-bd03b6b7e0bc"),"string2ea4f79e",VString,"seen"))
    val variableRecord = VariableRecord(new Date(), template1, variables)
    clickhouseConnector.insert(variableRecord).futureValue
  }
}
