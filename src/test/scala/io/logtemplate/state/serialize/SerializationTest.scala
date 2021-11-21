package io.logtemplate.state.serialize

import io.circe.parser
import org.scalatest.funsuite.AnyFunSuite

class SerializationTest extends AnyFunSuite {
  test("deserialize state") {
    val deserialized = parser.parse("""{"lengthMap":{"6":{"children":{"User":{"children":{"<*>":{"templates":{"2ea4f76d-fed3-4489-9173-bd03b6b7e0bc":{"id":"2ea4f76d-fed3-4489-9173-bd03b6b7e0bc","tokens":[{"FreeToken":{"s":"User"}},{"WildCardToken":{}},{"FreeToken":{"s":"has"}},{"WildCardToken":{}},{"FreeToken":{"s":"and"}},{"WildCardToken":{}}]}}}}}}}}}""".stripMargin)
    assert(deserialized.isRight)
  }
}
