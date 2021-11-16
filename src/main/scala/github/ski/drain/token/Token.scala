package github.ski.drain.token

import java.util.UUID

sealed trait Token {
  def print(): String
}
sealed trait TemplateToken extends Token
sealed trait StructuredLogToken extends Token
sealed trait ComparableToken extends Token

case class EnclosedToken(s: String) extends StructuredLogToken {
  def print() = s
}

case class FreeToken(s: String) extends TemplateToken with StructuredLogToken with ComparableToken {
  def print() = s
}

case class ValueToken(v: String) extends StructuredLogToken {
  def print() = v
}

case class NamedValueToken(name: String, separator: String, value: String) extends StructuredLogToken {
  def print() = s"$name$separator$value"
}

case class VariableToken(id: UUID) extends TemplateToken {
  def print() = s"<$id>"
}

case class NamedVariableToken(id: UUID, name: String, inter: String) extends TemplateToken {
  def print() = s"<$id,$name,$inter>"
}

case object WildCardToken extends ComparableToken {
  def print() = s"<*>"
}

object Token {
  def toComparable(t: Token): ComparableToken = {
    t match {
      case _: EnclosedToken => WildCardToken
      case FreeToken(s) => FreeToken(s)
      case _: ValueToken => WildCardToken
      case _: NamedValueToken => WildCardToken
      case _: VariableToken => WildCardToken
      case _ => WildCardToken
    }
  }

  def toTemplate(t: Token): TemplateToken = {
    t match {
      case _: EnclosedToken => VariableToken(UUID.randomUUID())
      case FreeToken(s) => FreeToken(s)
      case _: ValueToken => VariableToken(UUID.randomUUID())
      case NamedValueToken(name, inter, _) => NamedVariableToken(UUID.randomUUID(), name, inter)
      case v: VariableToken => v
      case _ => VariableToken(UUID.randomUUID())
    }
  }
}