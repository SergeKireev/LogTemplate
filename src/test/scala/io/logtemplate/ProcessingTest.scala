package io.logtemplate

import io.logtemplate.token.VariableToken
import io.logtemplate.token.{FreeToken, ValueToken, VariableToken}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ProcessingTest extends AnyFunSuite with Matchers {
  test("test 3 successive templatizations") {
    val log1 = "User 123 has connected and seen"
    val log2 = "User 123 has disconnected and seen"
    val log3 = "User 123 has reconnected and unseen"
    val drain = new Drain()
    val preprocessed1 = drain.preprocess(log1)
    val (template1, processed1)  = drain.process(preprocessed1)
    val variable1 = template1.tokens(1).asInstanceOf[VariableToken]
    assert(template1.print() === s"User <${variable1.id}> has connected and seen")
    processed1 should contain theSameElementsInOrderAs
      List(FreeToken("User"), ValueToken("123"), FreeToken("has"),
        FreeToken("connected"), FreeToken("and"), FreeToken("seen"))

    val preprocessed2 = drain.preprocess(log2)
    val (template2, processed2)  = drain.process(preprocessed2)
    val variable2 = template2.tokens(3).asInstanceOf[VariableToken]
    assert(template2.print() === s"User <${variable1.id}> has <${variable2.id}> and seen")
    processed2 should contain theSameElementsInOrderAs
      List(FreeToken("User"), ValueToken("123"), FreeToken("has"),
        ValueToken("disconnected"), FreeToken("and"), FreeToken("seen"))

    val preprocessed3 = drain.preprocess(log3)
    val (template3, processed3)  = drain.process(preprocessed3)
    val variable3 = template3.tokens(5).asInstanceOf[VariableToken]
    assert(template3.print() === s"User <${variable1.id}> has <${variable2.id}> and <${variable3.id}>")
    processed3 should contain theSameElementsInOrderAs
      List(FreeToken("User"), ValueToken("123"), FreeToken("has"), ValueToken("reconnected"),
        FreeToken("and"), ValueToken("unseen"))
  }
}
