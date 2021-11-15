package github.ski.drain.domain.template

import github.ski.drain.token.{FreeToken, StructuredLogToken, TemplateToken, Token, ValueToken, WildCardToken}

import java.util.UUID

case class Template(id: UUID, tokens: List[TemplateToken]) {
  def compareTokens(a: Token, b: Token): Boolean = {
    (a, b) match {
      case (FreeToken(a), FreeToken(b)) => a.trim == b.trim
      case _ => true
    }
  }

  def similarity(b: Template): Double = {
    val sumScore = this.tokens.zip(b.tokens).foldLeft(0) {
      case (acc, (a, b)) => acc + (if (compareTokens(a, b)) 1 else 0)
    }
    sumScore.toDouble / this.tokens.size
  }

  def maskBy(b: Template): Template = {
    val newTokens = tokens.zip(b.tokens).map {
      case (FreeToken(a), FreeToken(b)) =>
          if (a != b) WildCardToken else FreeToken(a)
      case (WildCardToken, _) =>
        WildCardToken
      case (_, WildCardToken) =>
        WildCardToken
    }
    this.copy(tokens = newTokens)
  }

  def mask(structured: List[StructuredLogToken]): List[StructuredLogToken] = {
    tokens.zip(structured).map {
      case (WildCardToken, FreeToken(s)) => ValueToken(s)
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
