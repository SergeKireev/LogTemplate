package io.logtemplate.`export`.index

import io.logtemplate.domain.template.Template

import java.util.UUID

trait IndexConnector[F[_]] {
  /**
   * Create index
   * @return
   */
  def init(): F[Unit]

  /**
   * Insert template into index
   * @param template
   * @return
   */
  def insert(template: Template): F[Unit]

  /**
   * Inserts multiple templates at a time
   * @param templates
   * @return
   */
  def insertBulk(templates: List[Template]): F[Unit]

  /**
   * Fetches a document by id
   * @param id
   * @return
   */
  def fetchTemplate(id: UUID): F[Option[Template]]

  /**
   * Fetches all documents
   * @param id
   * @return
   */
  def fetchTemplates(): F[List[Template]]
}
