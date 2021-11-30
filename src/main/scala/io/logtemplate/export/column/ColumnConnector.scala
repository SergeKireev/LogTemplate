package io.logtemplate.`export`.column

import io.logtemplate.domain.template.Template
import io.logtemplate.domain.template.{Template, Variable}

import java.util.{Date, UUID}

case class VariableRecord(date: Date, template: Template, variables: List[Variable])

trait ColumnConnector[F[_]] {
  def init(): F[Unit]
  def insertVariables(variableRecords: Seq[VariableRecord]): F[Unit]
  def insertTemplates(templates: List[Template]): F[Unit]
  def getForTemplateId(id: UUID): F[List[Any]]
}
