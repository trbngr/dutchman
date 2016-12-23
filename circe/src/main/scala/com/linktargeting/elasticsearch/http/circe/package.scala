package com.linktargeting.elasticsearch.http

import cats.syntax.either._
import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.api.translation._
import com.linktargeting.elasticsearch.http.marshalling._
import io.circe._
import io.circe.parser._

package object circe {

  implicit object CirceMarshaller extends ApiMarshaller {
    import translation.apiData

    def stringify(api: Api) = {
      api match {
        case bulk: Bulk ⇒
          val containers = apiData(bulk).get("actions") collect {
            case c: Seq[_] ⇒ c.asInstanceOf[BulkContainers]
          } getOrElse (throw new RuntimeException("invalid bulk data"))

          containers.map(_.toJson) mkString("", "\n", "\n")
        case _: Get     ⇒ ""
        case _: Delete  ⇒ ""
        case _          ⇒ apiData(api).toJson
      }
    }
  }

  implicit class RichStringMap(map: DataContainer) {
    def toJson = createJson(map).noSpaces

    private def createJson(value: Any): Json = value match {
      case v: String      ⇒ Json.fromString(v)
      case v: Boolean     ⇒ Json.fromBoolean(v)
      case v: Int         ⇒ Json.fromInt(v)
      case v: Long        ⇒ Json.fromLong(v)
      case v: Float       ⇒ Json.fromDoubleOrNull(v)
      case v: Double      ⇒ Json.fromDoubleOrNull(v)
      case v: BigInt      ⇒ Json.fromBigInt(v)
      case v: BigDecimal  ⇒ Json.fromBigDecimal(v)
      case v: Map[_, _]   ⇒ Json.obj(v.asInstanceOf[DataContainer].map(x ⇒ x._1 → createJson(x._2)).toSeq: _*)
      case v: Iterable[_] ⇒ Json.arr(v.map(createJson).toSeq: _*)
      case v              ⇒ throw new IllegalArgumentException(s"Unsupported scalar value: $v [${v.getClass}]")
    }
  }

  implicit object CirceUnmarshaller extends ApiUnMarshaller[Json] {

    import circe.codecs._
    import circe.syntax._

    private def e(op: String) = throw DecodingError(s"Invalid response: $op")

    def read(json: String) = {
      val j = parse(json).getOrElse(Json.Null)
      //      println(j)
      j
    }
    def error(json: String) = {
      println(s"error: $json")
      read(json).as[ESError].getOrElse(e("error"))
    }
    def index(json: Json) = {
      IndexResponse(
        response = json.as[Response].getOrElse(e("index.response")),
        created = json \ "created" bool false
      )
    }
    def deleteIndex(json: Json) = json.as[DeleteIndexResponse].getOrElse(e("deleteIndex"))
    def readError(json: Json) = {
      val errors = json \ "errors" bool false
      None
    }
    def bulk(json: Json) = json \ "items" bulkResponses
    override def search(json: Json) = json.as[SearchResponse[Json]].getOrElse(e("search"))
    override def refresh(json: Json) = json.as[RefreshResponse].getOrElse(e("refresh"))
    override def scroll(json: Json) = json.as[ScrollResponse[Json]].getOrElse(e("scroll"))
  }
}
