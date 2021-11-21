package io.logtemplate

import com.typesafe.scalalogging.LazyLogging
import io.logtemplate.domain.rule.RuleEngine
import io.logtemplate.domain.template.Template
import io.logtemplate.state.DrainStateController
import io.logtemplate.token.ExceptionAwareTokenizer
import io.logtemplate.domain.rule.{IgnoreMatch, NoMatch, RuleEngine, UserRule, UserRuleNamedValueMatch, UserRuleValueMatch}
import io.logtemplate.domain.template.{Template, VString, Variable}
import io.logtemplate.state.{DrainConfig, DrainState, DrainStateController}
import io.logtemplate.token.{BracketAwareTokenizer, EnclosedToken, ExceptionAwareTokenizer, FreeToken, NamedValueToken, SimpleTokenizer, StructuredLogToken, ValueToken, VariableToken}
import io.logtemplate.util.CommonRules

case class DrainResult(template: String, variables: Map[Int, String])

class Drain(initialDrainState: DrainState = DrainState(), config: DrainConfig = DrainConfig()) extends LazyLogging {

  def tokenizer = config.tokenizeStrategy match {
    case "simple" => new SimpleTokenizer(" ")
    case "bracket-aware" => new BracketAwareTokenizer()
    case "exception-aware" => new ExceptionAwareTokenizer()
  }

  val drainState = initialDrainState

  val drainStateController = new DrainStateController(drainState, config)
  def userRules: List[UserRule] = CommonRules.DEFAULT_RULES

  /**
   * Preprocessing phase:
   *  - tokenize
   *  - apply user provided rules
   *  - identify template tokens
   * @param log
   * @return
   */
  def preprocess(content: String): List[StructuredLogToken] = {
    val tokens = tokenizer.tokenize(content)
    val ruleEngine = new RuleEngine
    tokens.flatMap {
      case FreeToken(s) =>
        ruleEngine.applyRules(s, userRules).subMatches.map {
          case UserRuleNamedValueMatch(name, sep, value) =>
            NamedValueToken(name, sep, value)
          case UserRuleValueMatch(value) =>
            ValueToken(value)
          case NoMatch(s) =>
            FreeToken(s)
          case IgnoreMatch(s) =>
            FreeToken(s)
        }
      case e => e :: Nil
    }.toList
  }

  /**
   * Using drain algorithm to extract
   * @param logEntry
   * @return
   */
  def process(content: List[StructuredLogToken]): (Template, List[StructuredLogToken]) = {
    drainStateController.upsert(content)
  }

  /**
   * Finish building the variables
   * @param template
   * @param structuredLog
   * @return
   */
  def postProcess(template: Template, structuredLog: List[StructuredLogToken]): List[Variable] = {
    val variables = structuredLog.zip(template.tokens).collect {
      case (EnclosedToken(s), VariableToken(id)) =>
        Variable(id, s"enclosed${id.toString.substring(0, 8)}", VString, s)
      case (ValueToken(s), VariableToken(id)) => {
        val varType = Variable.determineType(s)
        Variable(id, s"${varType.toName()}${id.toString.substring(0, 8)}", varType, s)
      }
      //TODO: Handle separator persistence
      case (NamedValueToken(name, separator, value), VariableToken(id)) => {
        val varType = Variable.determineType(value)
        Variable(id, name, varType, value)
      }
    }
    variables
  }

  /**
   * Log rebuild from template and variables
   */
  def rehydrate(template: Template, variables: List[Variable]): String = {
    variables.foldLeft(template.print()) {
      case (s, v) =>
        s.replace(s"<${v.id}>", v.value)
    }
  }
}
