package github.ski.drain.domain.template

import github.ski.drain.token.{ComparableToken, FreeToken, StructuredLogToken, TemplateToken, Token, ValueToken, VariableToken}

import java.util.UUID

case class Template(id: UUID, tokens: List[TemplateToken]) {
  def compareTokens(a: ComparableToken, b: ComparableToken): Boolean = {
    (a, b) match {
      case (FreeToken(a), FreeToken(b)) => a.trim == b.trim
      case _ => true
    }
  }

  def similarity(b: Template): Double = {
    val sumScore = this.tokens.zip(b.tokens).foldLeft(0) {
      case (acc, (a, b)) => acc + (if (compareTokens(Token.toComparable(a), Token.toComparable(b))) 1 else 0)
    }
    sumScore.toDouble / this.tokens.size
  }

  def maskBy(logTokens: List[StructuredLogToken]): Template = {
    val newTokens = tokens.zip(logTokens).map {
      case (FreeToken(a), FreeToken(b)) =>
          if (a != b) VariableToken(UUID.randomUUID()) else FreeToken(a)
      case (VariableToken(id), _) =>
        VariableToken(id)
      case (_, _) =>
        VariableToken(UUID.randomUUID())
    }
    this.copy(tokens = newTokens)
  }

  def mask(structured: List[StructuredLogToken]): List[StructuredLogToken] = {
    tokens.zip(structured).map {
      case (_: VariableToken, FreeToken(s)) => ValueToken(s)
      case (_, t: Token) => t
    }
  }

  def print(): String = {
    tokens.map(_.print()).mkString(" ")
  }

  def serialize(): String = {
    ""
  }

  def deserialize(): String = {
    ""
  }
}
