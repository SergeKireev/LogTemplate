package github.ski.drain.`import`.common

import com.typesafe.scalalogging.LazyLogging
import github.ski.drain.domain.log.LogEntry

import java.text.SimpleDateFormat
import scala.annotation.tailrec
import scala.util.{Success, Try}

sealed trait DissectSeparator
case class Separator(s: String) extends DissectSeparator
case class ToMatch(s: String) extends DissectSeparator
case object Start extends DissectSeparator
case object End extends DissectSeparator

class Dissect(pattern: DissectPattern) extends LazyLogging {
  private val VARIABLE_START_R = "\\%\\{"
  private val VARIABLE_END_R = "\\}"
  private val VARIABLE_START = "%{"
  private val VARIABLE_END = "}"

  @tailrec
  private def compilePatternHelper(leftString: String, inside: Boolean, acc: List[DissectSeparator]): List[DissectSeparator] = {
    if (inside) {
      val name :: tail = leftString.split(VARIABLE_END_R).toList
      compilePatternHelper(tail.mkString(VARIABLE_END), false, acc :+ ToMatch(name))
    } else {
      val tokens: List[String] = leftString.split(VARIABLE_START_R).toList
      tokens match {
        case hd :: Nil =>
          acc :+ Separator(hd)
        case hd :: tl =>
          compilePatternHelper(tl.mkString(VARIABLE_START), true, acc :+ Separator(hd))
      }
    }
  }

  def compilePattern(): List[DissectSeparator] = {
    logger.info(s"compiling pattern $pattern")
    (Start :: (compilePatternHelper(pattern.pattern, false, Nil) :+ End))
  }

  @tailrec
  private def matchPatternHelper(leftString: String,
                                 tokens: List[DissectSeparator],
                                 acc: Map[String, String]): Map[String, String] = {
    tokens match {
      case Start :: Separator(s) :: tail =>
        matchPatternHelper(leftString.drop(s.size), tail, acc)
      case ToMatch(name) :: Separator(s) :: tail =>
        val startOfSeparator = leftString.indexOf(s)
        val newAcc = acc + (name -> leftString.take(startOfSeparator))
        val newLeftString = leftString.drop(startOfSeparator+s.size)
        matchPatternHelper(newLeftString, tail, newAcc)
      case ToMatch(name) :: End :: Nil =>
        acc + (name -> leftString)
      case End :: Nil =>
        acc
    }
  }

  def extractLogEvent(s: String): Try[LogEntry] = {
    val tokensToMatch = compilePattern()
    val valueMap = matchPatternHelper(s, tokensToMatch, Map.empty[String, String])
    val ts = valueMap("ts")
    val msg = valueMap("msg")
    val simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd")
    val date = simpleDateFormat.parse(ts)
    Success(LogEntry(date, valueMap - "ts" - "msg", msg))
  }
}
