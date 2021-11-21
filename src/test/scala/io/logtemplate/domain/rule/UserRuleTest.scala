package io.logtemplate.domain.rule

import io.logtemplate.util.CommonRules
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.util.UUID

class UserRuleTest extends AnyFunSuite with Matchers {
  test("equality regex") {
    val token = "xxx=yyy"
    val rule = CommonRules.EQUALITY_RULE
    val result = rule(token)
    val expected = (UserRuleResult(UserRuleNamedValueMatch(name = "xxx", separator = "=", value= "yyy") :: Nil))
    assert(result === expected)
  }

  test("equality regex 2") {
    val token = "environment:zookeeper.version=3.4.9-1757313"
    val rule = CommonRules.EQUALITY_RULE
    val result = rule(token)
    val expected = (UserRuleResult(UserRuleNamedValueMatch(name = "environment:zookeeper.version", separator = "=", value= "3.4.9-1757313") :: Nil))
    assert(result === expected)
  }

  test("commit hash regex") {
    val token = "77a89fcf8d7fa018"
    val rule = CommonRules.COMMIT_HASH_RULE
    val result = rule(token)
    val expected = (UserRuleResult(UserRuleValueMatch(value = token) :: Nil))
    assert(result === expected)
  }

  test("hexa regex") {
    val token = "0x77a89fcf8d7fa018"
    val rule = CommonRules.HEXA_RULE
    val result = rule(token)
    val expected = (UserRuleResult(UserRuleValueMatch(value = token) :: Nil))
    assert(result === expected)
  }

  test("uuid regex") {
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
