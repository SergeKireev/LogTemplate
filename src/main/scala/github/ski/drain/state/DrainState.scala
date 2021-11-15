package github.ski.drain.state

import github.ski.drain.domain.template.Template
import github.ski.drain.state.DrainState.DEFAULT_MAX_DEPTH
import io.circe.{Decoder, parser}
import io.circe.generic.semiauto.deriveDecoder
import io.circe.syntax._

import java.util.UUID
import scala.collection.mutable
import github.ski.drain.state.serialize.DrainStateCodec._

sealed trait PrefixTree
case class PrefixTreeInternal(children: mutable.Map[String, PrefixTree] = mutable.Map.empty[String, PrefixTree]) extends PrefixTree
case class PrefixTreeLeaf(templates: mutable.Map[UUID, Template] = mutable.Map.empty[UUID, Template]) extends PrefixTree

case class DrainConfig(maxDepth: Int = DEFAULT_MAX_DEPTH, similarityThreshold: Float = 0.5f)

case class DrainState(lengthMap: mutable.Map[Int, PrefixTree])

object DrainState {
  val DEFAULT_MAX_DEPTH = 1
  val DEFAULT_SIMILARITY_THRESHOLD = 0.5

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