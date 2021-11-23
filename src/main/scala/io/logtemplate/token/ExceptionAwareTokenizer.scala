package io.logtemplate.token

import com.typesafe.scalalogging.LazyLogging

class ExceptionAwareTokenizer(delegate: Tokenizer, exceptionPattern: String) extends Tokenizer with LazyLogging {
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
