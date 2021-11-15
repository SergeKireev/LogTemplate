package github.ski.drain.token

class SimpleTokenizer(separatorChars: String) extends Tokenizer {
  override def tokenize(s: String): Array[StructuredLogToken] = {
    s.split(separatorChars).map(FreeToken)
  }
}
