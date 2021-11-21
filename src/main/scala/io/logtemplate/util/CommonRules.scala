package io.logtemplate.util

import io.logtemplate.domain.rule.IgnoreRegexUserRule
import io.logtemplate.domain.rule.{IgnoreRegexUserRule, NamedValueRegexUserRule, ValueRegexUserRule}


object CommonRules {
  val SHORT = "(1[0-9][0-9]|2[0-4][0-9]|25[0-5]|[1-9][0-9]|[0-9])"
  val IPV4_PATTERN = s"$SHORT\\.$SHORT\\.$SHORT\\.$SHORT(:([0-9]{1,5}))?"
  val NUMBER_PATTERN = "(\\-?[0-9]+(\\.[0-9]*)?)"
  val IDENTIFIER_PATTERN = "[a-zA-Z0-9].+[0-9]+[a-zA-Z0-9].*[0-9]+[a-zA-Z0-9].+"
  val UUID_PATTERN = "(([a-f]|[0-9]){8}\\-([a-f]|[0-9]){4}\\-([a-f]|[0-9]){4}\\-([a-f]|[0-9]){4}\\-([a-f]|[0-9]){12})"
  val COMMIT_HASH_PATTERN = s"(([0-9]|[a-f]){16})"
  val DATE_PATTERN = "([0-9][0-9])/([0-9][0-9])/([0-9][0-9][0-9][0-9])"
  val HEXACHAR = "[0-9]|[a-f]"
  val HEXA_PATTERN = s"(0x([0-9]|[a-f])+)"
  val EQUALITY_PATTERN_LEFT_HAND_GROUP = 1
  val EQUALITY_PATTERN_RIGHT_HAND_GROUP = 2
  val EQUALITY_PATTERN = s"(.+)=(.+)"
  val EXCEPTION_PATTERN = s"""	at.*"""

  val IPV4_RULE = ValueRegexUserRule(regex = IPV4_PATTERN.r, valueGroup = 0)
  val UUID_RULE = ValueRegexUserRule(regex = UUID_PATTERN.r, valueGroup = 0)
  val NUMBER_RULE = ValueRegexUserRule(regex = NUMBER_PATTERN.r, valueGroup = 0)
  val EQUALITY_RULE = NamedValueRegexUserRule(regex = EQUALITY_PATTERN.r, nameGroup = 0, valueGroup = 1)
  val COMMIT_HASH_RULE = ValueRegexUserRule(regex = COMMIT_HASH_PATTERN.r, valueGroup = 0)
  val DATE_RULE = ValueRegexUserRule(regex = DATE_PATTERN.r, valueGroup = 0)
  val HEXA_RULE = ValueRegexUserRule(regex = HEXA_PATTERN.r, valueGroup = 0)
  val EXCEPTION_RULE = IgnoreRegexUserRule(regex = EXCEPTION_PATTERN.r)
  val IDENTIFIER_RULE = ValueRegexUserRule(regex = IDENTIFIER_PATTERN.r, valueGroup = 0)

  val DEFAULT_RULES = List(
    EXCEPTION_RULE,
    EQUALITY_RULE,
    UUID_RULE,
    IPV4_RULE,
    COMMIT_HASH_RULE,
    HEXA_RULE,
    IDENTIFIER_RULE,
    DATE_RULE,
    NUMBER_RULE)
}