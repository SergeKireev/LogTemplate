package io.logtemplate.token

import Tokenizer.DEFAULT_SEPARATORS

import scala.collection.mutable.ListBuffer
class BracketAwareTokenizer(separators: String = DEFAULT_SEPARATORS) extends Tokenizer {

  def separateBoundaries(s: String, openingChar: Char, closingChar: Char): List[StructuredLogToken] = {
    var count = 0
    var buffer: StringBuffer = new StringBuffer()
    val strings: ListBuffer[StructuredLogToken] = new ListBuffer[StructuredLogToken]
    s.foreach {
      case c if (c == openingChar) =>
        if (count == 0) {
          strings += FreeToken(buffer.toString)
          buffer = new StringBuffer()
        }
        count = count + 1
        buffer.append(c)

      case c if (c == closingChar) =>
        buffer.append(c)
        if (count > 0)
          count = count - 1
        if (count == 0) {
          strings += EnclosedToken(buffer.toString)
          buffer = new StringBuffer()
        }

      case c => buffer.append(c)
    }
    if (buffer.toString.nonEmpty)
      strings += FreeToken(buffer.toString)
    strings.toList
  }

  override def tokenize(s: String): Array[StructuredLogToken] = {
    val preTokens = separateBoundaries(s, '{', '}')
    preTokens.flatMap {
      case FreeToken(s) => s.split(separators).map(FreeToken)
      case e => e :: Nil
    }.toArray
  }
}
