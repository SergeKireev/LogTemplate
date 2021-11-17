package github.ski.drain.`export`.index

import github.ski.drain.domain.template.Template

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
   * Fetches a document by id
   * @param id
   * @return
   */
  def fetchDocument(id: UUID): F[Option[Template]]
}
