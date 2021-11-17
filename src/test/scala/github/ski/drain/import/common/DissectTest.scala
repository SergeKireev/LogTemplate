package github.ski.drain.`import`.common

import github.ski.drain.domain.log.LogEntry
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.util.Success

class DissectTest extends AnyFunSuite with Matchers {
  test("compile dissect pattern") {
    val pattern = DissectPattern("%{foo} %{bar},%{baz}[%{bag}]")
    val dissect = new Dissect(pattern)
    val tokens = dissect.compilePattern()
    tokens should contain theSameElementsInOrderAs
      List(
        Start,
        Separator(""),
        ToMatch("foo"),
        Separator(" "),
        ToMatch("bar"),
        Separator(","),
        ToMatch("baz"),
        Separator("["),
        ToMatch("bag"),
        Separator("]"),
        End
      )
  }

  test("match dissect pattern") {
    val pattern = DissectPattern("%{ts} %{log_level},%{pid}[%{msg}]")
    val log = "2020-08-01 [DEBUG],111[Well hello there]"
    val dissect = new Dissect(pattern)
    val Success(logEntry) = dissect.extractLogEvent(log)
    val expected = LogEntry(logEntry.date,
      Map(
        "log_level" -> "[DEBUG]",
        "pid" -> "111",
      ),
    "Well hello there")
    assert(logEntry == expected)
  }
}
