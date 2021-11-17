package github.ski.drain.state.serialize

import github.ski.drain.domain.template.{Template, Variable}
import github.ski.drain.state.{DrainConfig, DrainState, DrainStateController}
import github.ski.drain.token.{FreeToken, VariableToken}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.util.UUID
import scala.util.Random

class RebuildStateTest extends AnyFunSuite with Matchers {
  def uuidGen(random: Random) = {
    new UUID(random.nextLong(), random.nextLong())
  }

  test("rebuild with a list of templates") {
    val drainState = DrainState()
    val drainStateController = new DrainStateController(drainState, DrainConfig())
    val random = new Random(0L)
    val templateId1 = uuidGen(random)
    val varId1 = uuidGen(random)
    val varId2 = uuidGen(random)
    val templateId2 = uuidGen(random)
    val varId3 = uuidGen(random)
    val templateId3 = uuidGen(random)
    val varId4 = uuidGen(random)
    val template1 = Template(id = templateId1, tokens = List(FreeToken("User"), VariableToken(varId1), FreeToken("has"), VariableToken(varId2)))
    val template2 = Template(id = templateId2, tokens = List(FreeToken("User"), VariableToken(varId3), FreeToken("not"), FreeToken("authenticated")))
    val template3 = Template(id = templateId3, tokens = List(FreeToken("User"), FreeToken("New"), FreeToken("connection"), VariableToken(varId4)))
    drainStateController.insertTemplate(template1)
    drainStateController.insertTemplate(template2)
    drainStateController.insertTemplate(template3)
  }
}
