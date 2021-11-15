package github.ski.drain.util

import github.ski.drain.domain.rule.{NamedValueRegexUserRule, ValueRegexUserRule}


object CommonRules {
  val SHORT = "(1[0-9][0-9]|2[0-4][0-9]|25[0-5]|[1-9][0-9]|[0-9])"
  val IPV4_PATTERN = s"$SHORT\\.$SHORT\\.$SHORT\\.$SHORT(:([0-9]{1,4}))?"
  val NUMBER_PATTERN = "(\\-?[0-9]+(\\.[0-9]*)?)"
  val UUID_PATTERN = "(([a-f]|[0-9]){8}\\-([a-f]|[0-9]){4}\\-([a-f]|[0-9]){4}\\-([a-f]|[0-9]){4}\\-([a-f]|[0-9]){12})"
  val EQUALITY_PATTERN_LEFT_HAND_GROUP = 1
  val EQUALITY_PATTERN_RIGHT_HAND_GROUP = 2
  val EQUALITY_PATTERN = s"(.*)=(.*)"

  val IPV4_RULE = ValueRegexUserRule(regex = IPV4_PATTERN.r, valueGroup = 0)
  val UUID_RULE = ValueRegexUserRule(regex = UUID_PATTERN.r, valueGroup = 0)
  val NUMBER_RULE = ValueRegexUserRule(regex = NUMBER_PATTERN.r, valueGroup = 0)
  val EQUALITY_RULE = NamedValueRegexUserRule(regex = EQUALITY_PATTERN.r, nameGroup = 0, valueGroup = 1)

  val DEFAULT_RULES = List(IPV4_RULE, UUID_RULE, NUMBER_RULE, EQUALITY_RULE)
}