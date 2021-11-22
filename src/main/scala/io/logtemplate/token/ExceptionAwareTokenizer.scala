package io.logtemplate.token

class ExceptionAwareTokenizer(delegate: Tokenizer, exceptionPattern: String) extends Tokenizer {
  override def tokenize(s: String): Array[StructuredLogToken] = {
    val startOfException = s.indexOf(exceptionPattern)
    if (startOfException>=0) {
      val exception = s.substring(startOfException)
      val before = s.substring(0, startOfException)
      val tokens = delegate.tokenize(before)
      tokens :+ EnclosedToken(exception)
    } else {
      delegate.tokenize(s)
    }
  }
}
