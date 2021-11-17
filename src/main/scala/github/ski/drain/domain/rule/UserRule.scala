package github.ski.drain.domain.rule

import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

trait UserRuleSubMatch
case class UserRuleNamedValueMatch(name: String, separator: String, value: String) extends UserRuleSubMatch
case class UserRuleValueMatch(value: String) extends UserRuleSubMatch
case class NoMatch(s: String) extends UserRuleSubMatch

case class UserRuleResult(subMatches: List[UserRuleSubMatch])

trait Boundaries {
  val start: Int
  val end: Int
}
case class NameBoundaries(start: Int, end: Int) extends Boundaries
case class ValueBoundaries(start: Int, end: Int) extends Boundaries

trait UserRule {
  def apply(s: String): UserRuleResult
}

/**
 * We consider that there may not be multiple named values inside a token
 */
case class NamedValueRegexUserRule(regex: Regex, nameGroup: Int, valueGroup: Int) extends UserRule {
  def apply(s: String): UserRuleResult = {
    val subMatches = regex.findFirstMatchIn(s).toList.flatMap {
      case m: Match =>
        val name = m.subgroups(nameGroup)
        val value = m.subgroups(valueGroup)
        val startName = m.start(nameGroup+1)
        val endName = m.end(nameGroup+1)
        val startValue = m.start(valueGroup+1)
        val endValue = m.end(valueGroup+1)
        (NoMatch(s.substring(0, startName)) ::
          UserRuleNamedValueMatch(name, s.substring(endName, startValue), value) ::
          NoMatch(s.substring(endValue)) :: Nil)
    }
    val nonEmpty = subMatches.filter {
      case NoMatch(s) => s.nonEmpty
      case _ => true
    }
    nonEmpty.headOption.map {
      _ =>
        UserRuleResult(nonEmpty)
    }.getOrElse {
      UserRuleResult(NoMatch(s) :: Nil)
    }
  }
}

case class ValueRegexUserRule(regex: Regex, valueGroup: Int) extends UserRule {
  def apply(s: String): UserRuleResult = {
    val matchBoundaries: List[Boundaries] = (regex.findAllIn(s).matchData.toList.map {
      case m: Match =>
        ValueBoundaries(m.start(valueGroup), m.end(valueGroup))
    })
    val sorted = matchBoundaries.sortBy(x => x.start)
    var current = 0
    val buffer = new ListBuffer[UserRuleSubMatch]
    sorted.foreach {
      case v: ValueBoundaries =>
        buffer += NoMatch(s.substring(current, v.start))
        buffer += UserRuleValueMatch(s.substring(v.start, v.end))
        current = v.end
    }
    buffer += NoMatch(s.substring(current))
    val nonEmpty = buffer.filter {
      case NoMatch(s) => s.nonEmpty
      case _ => true
    }
    UserRuleResult(nonEmpty.toList)
  }
}