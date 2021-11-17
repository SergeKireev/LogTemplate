package github.ski.drain.domain.template

import io.circe.Json
import io.circe.syntax._

import java.util.UUID
import scala.util.Try

sealed trait VariableType {
  def toValue(s: String): Any
  def toJson(s: String): Json
  def toName(): String
}
case object VString extends VariableType {
  def toValue(s: String): String = {
    s
  }

  def toName(): String = {
    "string"
  }

  override def toJson(s: String): Json = {
    s.asJson
  }
}
case object VLong extends VariableType {
  def toValue(s: String): Long = {
    s.toLong
  }

  def toName(): String = {
    "int"
  }

  override def toJson(s: String): Json = {
    s.toLong.asJson
  }
}
case object VDouble extends VariableType {
  override def toValue(s: String): Any = {
    s.toDouble
  }

  def toName(): String = {
    "float"
  }

  override def toJson(s: String): Json = {
    s.toDouble.asJson
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