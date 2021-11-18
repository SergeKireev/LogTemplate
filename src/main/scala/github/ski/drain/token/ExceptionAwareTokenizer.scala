package github.ski.drain.token

class ExceptionAwareTokenizer extends Tokenizer {
  private val delegate = new BracketAwareTokenizer
  override def tokenize(s: String): Array[StructuredLogToken] = {
    val startOfException = s.indexOf("	at")
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
