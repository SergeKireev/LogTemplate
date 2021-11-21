package io.logtemplate.token

object Tokenizer {
  val DEFAULT_SEPARATORS = "( |\\,)"
}

trait Tokenizer {
  def tokenize(s: String): Array[StructuredLogToken]
}
