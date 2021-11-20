package github.ski.drain

import com.typesafe.scalalogging.LazyLogging
import github.ski.drain.domain.rule.{IgnoreMatch, NoMatch, RuleEngine, UserRule, UserRuleNamedValueMatch, UserRuleValueMatch}
import github.ski.drain.domain.template.{Template, VString, Variable}
import github.ski.drain.state.{DrainConfig, DrainState, DrainStateController}
import github.ski.drain.token.{BracketAwareTokenizer, EnclosedToken, ExceptionAwareTokenizer, FreeToken, NamedValueToken, SimpleTokenizer, StructuredLogToken, Token, Tokenizer, ValueToken, VariableToken}
import github.ski.drain.util.CommonRules

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
