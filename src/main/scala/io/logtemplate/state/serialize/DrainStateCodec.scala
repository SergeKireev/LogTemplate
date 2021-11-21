package io.logtemplate.state.serialize

import io.logtemplate.state.DrainState
import io.circe.{Codec, Decoder, DecodingFailure, Encoder, HCursor, Json, JsonObject}
import io.circe.generic.semiauto.{deriveCodec, deriveEncoder}
import io.circe.syntax._
import io.logtemplate.domain.template.Template
import io.logtemplate.state.{DrainState, PrefixTree, PrefixTreeInternal, PrefixTreeLeaf}
import io.logtemplate.token.TemplateToken

import java.util.UUID
import scala.collection.mutable

object DrainStateCodec {

  implicit lazy val templateTokenCodec: Codec[TemplateToken] = deriveCodec[TemplateToken]
  implicit lazy val templateCodec: Codec[Template] = deriveCodec[Template]
  implicit lazy val prefixTreeLeafCodec: Codec[PrefixTreeLeaf] = deriveCodec[PrefixTreeLeaf]

  implicit lazy val prefixTreeEnc: Encoder[PrefixTree] = Encoder.instance[PrefixTree] {
    case PrefixTreeInternal(children) =>
      val obj = JsonObject.empty
      obj.add("children", Json.fromFields(children.map {
        case (field, value: PrefixTreeInternal) => (field, prefixTreeEnc(value))
        case (field, value: PrefixTreeLeaf) => (field, prefixTreeLeafCodec(value))
      }.toMap)).asJson
    case p: PrefixTreeLeaf => prefixTreeLeafCodec(p)
  }

  implicit val prefixTreeDec: Decoder[PrefixTree] = Decoder.instance[PrefixTree] {
    case obj: HCursor => {
      val leaf = obj.downField("templates").as[mutable.Map[UUID, Template]]
      if (leaf.isLeft) {
        val childrenCursor = obj.downField("children")
        val keys = childrenCursor.keys.get
        val children = keys.flatMap {
          k =>
            childrenCursor.downField(k).as[PrefixTree](prefixTreeDec).toOption.map(x => (k, x))
        }
        Right(PrefixTreeInternal(children = mutable.Map.from(children)))
      } else leaf.map(PrefixTreeLeaf)
    }
  }

  implicit lazy val drainStateCodec: Codec[DrainState] = deriveCodec[DrainState]
}
