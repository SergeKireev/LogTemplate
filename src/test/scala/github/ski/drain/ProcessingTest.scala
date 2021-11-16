package github.ski.drain

import github.ski.drain.token.{BracketAwareTokenizer, FreeToken, ValueToken}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ProcessingTest extends AnyFunSuite with Matchers {
  test("test 3 successive templatizations") {
    val log1 = "User 123 has connected and seen"
    val log2 = "User 123 has disconnected and seen"
    val log3 = "User 123 has reconnected and unseen"
    val tokenizer = new BracketAwareTokenizer()
    val drain = new Drain(tokenizer)
    val preprocessed1 = drain.preprocess(log1)
    val (template1, processed1)  = drain.process(preprocessed1)
    assert(template1.print() === "User <*> has connected and seen")
    processed1 should contain theSameElementsInOrderAs
      List(FreeToken("User"), ValueToken("123"), FreeToken("has"),
        FreeToken("connected"), FreeToken("and"), FreeToken("seen"))

    val preprocessed2 = drain.preprocess(log2)
    val (template2, processed2)  = drain.process(preprocessed2)
    assert(template2.print() === "User <*> has <*> and seen")
    processed2 should contain theSameElementsInOrderAs
      List(FreeToken("User"), ValueToken("123"), FreeToken("has"),
        ValueToken("disconnected"), FreeToken("and"), FreeToken("seen"))

    val preprocessed3 = drain.preprocess(log3)
    val (template3, processed3)  = drain.process(preprocessed3)
    assert(template3.print() === "User <*> has <*> and <*>")
    processed3 should contain theSameElementsInOrderAs
      List(FreeToken("User"), ValueToken("123"), FreeToken("has"), ValueToken("reconnected"),
        FreeToken("and"), ValueToken("unseen"))
  }
}
