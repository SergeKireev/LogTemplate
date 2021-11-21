package io.logtemplate.domain.rule

class RuleEngine {

  def applyRules(s: String, rules: List[UserRule]): UserRuleResult = {
    val matches: List[UserRuleSubMatch] = NoMatch(s) :: Nil
    val subMatches = rules.foldLeft(matches) {
      case (acc, rule) =>
        val newMatches = acc.flatMap {
          case NoMatch(s) =>
            rule(s).subMatches
          case m => m :: Nil
        }
        newMatches
    }
    UserRuleResult(subMatches)
  }
}
