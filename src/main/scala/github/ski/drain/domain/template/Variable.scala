package github.ski.drain.domain.template

import java.util.UUID
import scala.util.Try

sealed trait VariableType {
  def toValue(s: String): Any
  def toName(): String
}
case object VString extends VariableType {
  def toValue(s: String): String = {
    s
  }

  def toName(): String = {
    "string"
  }
}
case object VLong extends VariableType {
  def toValue(s: String): Long = {
    s.toInt
  }

  def toName(): String = {
    "int"
  }
}
case object VDouble extends VariableType {
  override def toValue(s: String): Any = {
    s.toDouble
  }

  def toName(): String = {
    "float"
  }
}

case class Variable(id: UUID, name: String, `type`: VariableType, value: String)

object Variable {

  def determineType(s: String): VariableType = {
    val types = List(
      VLong,
      VString,
      VDouble
    )
    types.flatMap(t => Try(t.toValue(s)).toOption.map(_ => t)).head
  }
}