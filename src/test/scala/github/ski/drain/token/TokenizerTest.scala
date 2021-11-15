package github.ski.drain.token

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class TokenizerTest extends AnyFunSuite with Matchers {
  test("test enclosing brackets detection") {
    val example = """My test has an object json in it {"prop":{"hello":1}}"""
    val tokenizer = new BracketAwareTokenizer()
    val boundaries = tokenizer.separateBoundaries(example, '{', '}')
    boundaries should contain theSameElementsAs (List(FreeToken("My test has an object json in it "), EnclosedToken("""{"prop":{"hello":1}}""")))
  }
}
