package io.logtemplate.`import`.common

import com.typesafe.scalalogging.LazyLogging
import io.logtemplate.domain.log.LogEntry

import java.text.SimpleDateFormat
import scala.annotation.tailrec
import scala.util.{Success, Try}

sealed trait DissectSeparator
sealed trait DissectMatcher extends DissectSeparator {
  val s: String
}
case class Separator(s: String) extends DissectSeparator
case class ToMatch(s: String) extends DissectMatcher
case class AddingToMatch(s: String) extends DissectMatcher

case object Start extends DissectSeparator
case object End extends DissectSeparator

class Dissect(pattern: DissectPattern, dateFormat: String) extends LazyLogging {
  private val VARIABLE_START_R = "\\%\\{"
  private val VARIABLE_END_R = "\\}"
  private val VARIABLE_START = "%{"
  private val VARIABLE_END = "}"

  @tailrec
  private def compilePatternHelper(leftString: String, inside: Boolean, acc: List[DissectSeparator]): List[DissectSeparator] = {
    if (inside) {
      val matcher :: tail = leftString.split(VARIABLE_END_R).toList
      val newToken = matcher match {
        case s"+$name" => AddingToMatch(name)
        case s"$name" => ToMatch(name)
      }
      compilePatternHelper(tail.mkString(VARIABLE_END), false, acc :+ newToken)
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

  lazy val compilePattern: List[DissectSeparator] = {
    logger.debug(s"compiling pattern $pattern")
    (Start :: (compilePatternHelper(pattern.pattern, false, Nil) :+ End)).filter {
      case Separator(s) => s.nonEmpty
      case _ => true
    }
  }

  @tailrec
  private def matchPatternHelper(leftString: String,
                                 tokens: List[DissectSeparator],
                                 acc: Map[String, String]): Map[String, String] = {
    tokens match {
      case Start :: Separator(s) :: tail =>
        matchPatternHelper(leftString.drop(s.size), tail, acc)
      case Start :: tail =>
        matchPatternHelper(leftString, tail, acc)
      case (m: DissectMatcher) :: Separator(s) :: tail =>
        val name = m.s
        val startOfSeparator = leftString.indexOf(s)
        val matched = leftString.take(startOfSeparator)
        val newAcc = acc.updatedWith(name) {
          case x => m match {
            case AddingToMatch(s) =>
              Some((x ++ Option(matched)).mkString(" "))
            case ToMatch(s) =>
              Some(matched)
          }
        }
        val newLeftString = leftString.drop(startOfSeparator+s.size)
        matchPatternHelper(newLeftString, tail, newAcc)
      case ToMatch(name) :: End :: Nil =>
        acc + (name -> leftString)
      case End :: Nil =>
        acc
    }
  }

  def extractLogEvent(s: String): Try[LogEntry] = {
    logger.debug(s"Applying compiled pattern ${compilePattern} to $s")
    Try {
      val valueMap = matchPatternHelper(s, compilePattern, Map.empty[String, String])
      logger.debug(s"Value map ${valueMap}")
      val ts = valueMap("ts")
      val msg = valueMap("msg")
      val simpleDateFormat = new SimpleDateFormat(dateFormat)
      val date = simpleDateFormat.parse(ts)
      logger.debug(s"Parsing date $date from $ts")
      LogEntry(date, valueMap - "ts" - "msg", msg)
    }
  }
}
