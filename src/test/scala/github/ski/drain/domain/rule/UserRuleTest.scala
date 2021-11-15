package github.ski.drain.domain.rule

import github.ski.drain.util.CommonRules
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.util.UUID

class UserRuleTest extends AnyFunSuite with Matchers {
  test("test equality regex") {
    val token = "xxx=yyy"
    val rule = CommonRules.EQUALITY_RULE
    val result = rule(token)
    val expected = (UserRuleResult(UserRuleNamedValueMatch(name = "xxx", separator = "=", value= "yyy") :: Nil))
    assert(result === expected)
  }

  test("test uuid regex") {
    val token = UUID.randomUUID().toString
    val rule = CommonRules.UUID_RULE
    val result = rule(token)
    val expected = (UserRuleResult(UserRuleValueMatch(value=token) :: Nil))
    assert(result === expected)
  }

  test("test number regex") {
    val token1 = "123.25"
    val token2 = "-123.25"
    val token3 = "123"
    val rule = CommonRules.NUMBER_RULE
    val matches1 = rule(token1)
    val matches2 = rule(token2)
    val matches3 = rule(token3)
    val expected1 = (UserRuleResult(UserRuleValueMatch(value=token1) :: Nil))
    val expected2 = (UserRuleResult(UserRuleValueMatch(value=token2) :: Nil))
    val expected3 = (UserRuleResult(UserRuleValueMatch(value=token3) :: Nil))
    assert(matches1 === expected1)
    assert(matches2 === expected2)
    assert(matches3 === expected3)
  }
}
