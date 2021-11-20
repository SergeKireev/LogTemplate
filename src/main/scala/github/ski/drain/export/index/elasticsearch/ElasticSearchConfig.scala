package github.ski.drain.`export`.index.elasticsearch

import com.typesafe.config.Config
import github.ski.drain.util.ConfigOps._

class ElasticSearchConfig(config: Config) {
  def scoped: Config = config.getConfig("export.elasticsearch")

  def getHost() = scoped.getOrElse[String]("host", "localhost")
}
