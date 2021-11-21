package io.logtemplate.state

import com.typesafe.config.{Config, ConfigFactory}
import io.circe.{Decoder, parser}
import io.circe.generic.semiauto.deriveDecoder
import io.circe.syntax._

import java.util.UUID
import scala.collection.mutable
import io.logtemplate.state.serialize.DrainStateCodec._
import io.logtemplate.util.ConfigOps.WithFallback
import io.logtemplate.domain.template.Template

sealed trait PrefixTree
case class PrefixTreeInternal(children: mutable.Map[String, PrefixTree] = mutable.Map.empty[String, PrefixTree]) extends PrefixTree
case class PrefixTreeLeaf(templates: mutable.Map[UUID, Template] = mutable.Map.empty[UUID, Template]) extends PrefixTree

object DrainConfig {
  def apply(): DrainConfig = {
    DrainConfig(ConfigFactory.load())
  }
}

case class DrainConfig(config: Config) {
  val DEFAULT_MAX_DEPTH = 1
  val DEFAULT_SIMILARITY_THRESHOLD = 0.5f

  def scoped = config.getConfig("drain")
  def maxDepth: Int = scoped.getOrElse("max-depth", DEFAULT_MAX_DEPTH)
  def similarityThreshold: Float = scoped.getOrElse("similarity-threshold", DEFAULT_SIMILARITY_THRESHOLD)
  def tokenizeStrategy: String = scoped.getOrElse("tokenizer", "bracket-aware")
  def exportBatchSize: Int = scoped.getOrElse("export.batch-size", 1000)
}

case class DrainState(lengthMap: mutable.Map[Int, PrefixTree])

object DrainState {
  def apply(): DrainState = {
    DrainState(mutable.Map.empty)
  }

  def serialize(state: DrainState): String = {
    state.asJson.noSpaces
  }

  def deserialize(serialized: String): DrainState = {
    implicit val drainStateDecoder: Decoder[DrainState] = deriveDecoder[DrainState]
    parser.parse(serialized).flatMap(_.as[DrainState]) match {
      case Right(drainState: DrainState) =>
        drainState
    }
  }
}