package io.logtemplate

import io.logtemplate.token.ValueToken
import io.logtemplate.token.{BracketAwareTokenizer, EnclosedToken, FreeToken, ValueToken}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class PreprocessingTest extends AnyFunSuite with Matchers {
  test("test preprocessing of a value entry") {
    val content = "User [123][456] has connected"
    val tokenizer = new BracketAwareTokenizer()
    val drain = new Drain()
    val expected = List(FreeToken("User"),
      FreeToken("["),
      ValueToken("123"),
      FreeToken("]["),
      ValueToken("456"),
      FreeToken("]"),
      FreeToken("has"),
      FreeToken("connected"))

    drain.preprocess(content) should contain theSameElementsInOrderAs(expected)
  }

  test("test preprocessing of a json entry") {
    val content = """User {"user":"xxx", "error": "error"} is problematic"""
    val drain = new Drain()
    val expected = List(FreeToken("User"),
        EnclosedToken("""{"user":"xxx", "error": "error"}"""),
        FreeToken("is"),
        FreeToken("problematic"))

    drain.preprocess(content) should contain theSameElementsAs(expected)
  }

}
