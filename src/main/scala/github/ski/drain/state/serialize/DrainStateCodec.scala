package github.ski.drain.state.serialize

import github.ski.drain.domain.template.Template
import github.ski.drain.state.{DrainConfig, DrainState, PrefixTree, PrefixTreeInternal, PrefixTreeLeaf}
import github.ski.drain.token.TemplateToken
import io.circe.{Codec, Decoder, Encoder, HCursor, Json, JsonObject}
import io.circe.generic.semiauto.{deriveCodec, deriveEncoder}
import io.circe.syntax._

import java.util.UUID
import scala.collection.mutable

object DrainStateCodec {

  implicit lazy val templateTokenEnc: Codec[TemplateToken] = deriveCodec[TemplateToken]
  implicit lazy val templateEnc: Codec[Template] = deriveCodec[Template]
  implicit lazy val prefixTreeLeafEnc: Encoder[PrefixTreeLeaf] = deriveEncoder[PrefixTreeLeaf]

  implicit lazy val prefixTreeEnc: Encoder[PrefixTree] = Encoder.instance[PrefixTree] {
    case PrefixTreeInternal(children) =>
      val obj = JsonObject.empty
      obj.add("children", Json.fromFields(children.map {
        case (field, value: PrefixTreeInternal) => (field, prefixTreeEnc(value))
        case (field, value: PrefixTreeLeaf) => (field, prefixTreeLeafEnc(value))
      }.toMap)).asJson
    case p: PrefixTreeLeaf => prefixTreeLeafEnc(p)
  }

  implicit val prefixTreeDec: Decoder[PrefixTree] = Decoder.instance[PrefixTree] {
    case obj: HCursor => {
      val leaf = obj.downField("templates").as[Map[UUID, Template]].map {
        case templates => PrefixTreeLeaf(mutable.Map.from(templates))
      }
      if (leaf.isLeft) {
        val childrenCursor = obj.downField("children")
        val keys = childrenCursor.keys.get
        val children = keys.flatMap {
          k =>
            childrenCursor.downField(k).as[PrefixTree](prefixTreeDec).toOption.map(x => (k, x))
        }
        Right(PrefixTreeInternal(children = mutable.Map.from(children)))
      } else leaf
    }
  }

  implicit lazy val drainConfigEncoder: Codec[DrainConfig] = deriveCodec[DrainConfig]
  implicit lazy val drainStateEncoder: Codec[DrainState] = deriveCodec[DrainState]
}
