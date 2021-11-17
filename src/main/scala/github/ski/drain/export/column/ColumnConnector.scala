package github.ski.drain.`export`.column

import github.ski.drain.domain.template.{Template, Variable}

import java.util.{Date, UUID}

case class VariableRecord(date: Date, template: Template, variables: List[Variable])

trait ColumnConnector[F[_]] {
  def init(): F[Unit]
  def insert(variableRecord: VariableRecord): F[Unit]
  def getForTemplateId(id: UUID): F[List[Any]]
}
