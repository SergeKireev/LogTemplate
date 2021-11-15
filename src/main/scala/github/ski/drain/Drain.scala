package github.ski.drain

import github.ski.drain.domain.rule.{NoMatch, RuleEngine, UserRule, UserRuleNamedValueMatch, UserRuleValueMatch}
import github.ski.drain.domain.template.Template
import github.ski.drain.state.{DrainConfig, DrainState, DrainStateController}
import github.ski.drain.token.{FreeToken, NamedValueToken, StructuredLogToken, Token, Tokenizer, ValueToken}
import github.ski.drain.util.CommonRules

import java.util.UUID

case class DrainResult(template: String, variables: Map[Int, String])

class Drain(tokenizer: Tokenizer, initialDrainState: DrainState = DrainState(), config: DrainConfig = DrainConfig()) {

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
}
