package com.linktargeting.elasticsearch.http.circe

import cats.syntax.either._
import com.linktargeting.elasticsearch.api._
import io.circe.parser.parse
import io.circe.{ACursor, Decoder, DecodingFailure, Json}

private[circe] object syntax {
  implicit class jsonToOps(json: Json) {
    def \(field: String) = new JsonOps(json.hcursor.downField(field))
  }

  implicit class cursorToOps(c: ACursor) {
    def \(field: String) = new JsonOps(c.downField(field))
  }

  implicit class stringOps(rawJson: String) {
    def json = parse(rawJson).getOrElse(Json.Null)
    def as[T: Decoder] = json.as[T] match {
      case Right(x) ⇒ x
      case Left(f)  ⇒ throw f
    }
    def as[T: Decoder](default: T) = json.as[T].getOrElse(default)
  }

}

private[circe] final class JsonOps(c: ACursor) {

  import codecs._

  val cursor = c
  def string(default: String): String = as[String](default)
  def string: String = as[String]
  def optString: Option[String] = opt[String]

  def json(default: Json): Json = as[Json](default)
  def json: Json = as[Json]
  def optJson: Option[Json] = opt[Json]

  def bool(default: Boolean): Boolean = as[Boolean](default)
  def bool: Boolean = as[Boolean]
  def optBool: Option[Boolean] = opt[Boolean]

  def float: Float = as[Float]
  def float(default: Float): Float = as[Float](default)
  def optFloat: Option[Float] = opt[Float]

  def int: Any = as[Int]
  def int(default: Int): Int = as[Int](default)
  def optInt: Option[Int] = opt[Int]

  def shards: Shards = as[Shards](Shards(0, 0, 0))
  def response: Response = as[Response](Response(Shards(0, 0, 0), "", "", "", 0))
  def bulkResponses: Seq[BulkResponse] = as[Seq[BulkResponse]](Seq.empty)
  def documents: Seq[JsonDocument[Json]] = as[Seq[JsonDocument[Json]]](Seq.empty)

  def as[T: Decoder](default: T): T = c.as[T].getOrElse(default)
  def as[T: Decoder]: T = c.as[T].getOrElse(throw DecodingFailure("Required field not found.", c.history))
  def opt[T: Decoder]: Option[T] = c.as[Option[T]].getOrElse(Option.empty)

  def \(field: String) = new JsonOps(c.downField(field))
  def fields = c.fields
}
