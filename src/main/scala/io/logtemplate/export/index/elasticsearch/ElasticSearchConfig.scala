package io.logtemplate.`export`.index.elasticsearch

import com.typesafe.config.Config
import io.logtemplate.util.ConfigOps._

class ElasticSearchConfig(config: Config) {
  def scoped: Config = config.getConfig("export.elasticsearch")

  def getHost() = scoped.getOrElse[String]("host", "localhost")
}
