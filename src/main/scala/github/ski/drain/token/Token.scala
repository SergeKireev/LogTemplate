package github.ski.drain.token

sealed trait Token {
  def print(): String
}
sealed trait TemplateToken extends Token
sealed trait StructuredLogToken extends Token

case class EnclosedToken(s: String) extends StructuredLogToken {
  def print() = s
}

case class FreeToken(s: String) extends TemplateToken with StructuredLogToken {
  def print() = s
}

case class ValueToken(v: String) extends StructuredLogToken {
  def print() = v
}

case class NamedValueToken(name: String, separator: String, value: String) extends StructuredLogToken {
  def print() = s"$name$separator$value"
}

case object WildCardToken extends TemplateToken {
  def print() = s"<*>"
}

object Token {
  def toTemplate(t: Token) = {
    t match {
      case _: EnclosedToken => WildCardToken
      case FreeToken(s) => FreeToken(s)
      case _: ValueToken => WildCardToken
      case _: NamedValueToken => WildCardToken
      case WildCardToken => WildCardToken
    }
  }
}