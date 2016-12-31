package dutchman

import cats.syntax.either._
import dutchman.api._
import dutchman.marshalling._
import dutchman.marshalling.{ApiMarshaller, ApiUnMarshaller}
import dutchman.api.formatting.DataContainer
import io.circe._
import io.circe.parser._
import org.slf4j.LoggerFactory

package object circe {

  private val log = LoggerFactory.getLogger(getClass)

  implicit object CirceMarshaller extends ApiMarshaller {

    def marshal[A](api: Api[A]) = api match {
      case bulk: Bulk ⇒ bulk.bulkData.map(_.toJson) mkString("", "\n", "\n")
      case _: Get[_]     ⇒ ""
      case _: Delete  ⇒ ""
      case _          ⇒ api.data.toJson
    }
  }

  implicit class RichStringMap(map: DataContainer) {
    def toJson = {
      val json = createJson(map)
      log.debug(s"payload: $json")
      json.noSpaces
    }

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

    import codecs._
    import syntax._

    private def e(op: String) = throw DecodingError(s"Invalid response: $op")

    def read(json: String) = {
      val response = parse(json).getOrElse(Json.Null)
      log.debug(s"response: $response")
      response
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
    def search(json: Json) = json.as[SearchResponse[Json]].getOrElse(e("search"))
    def refresh(json: Json) = json.as[RefreshResponse].getOrElse(e("refresh"))
    def scroll(json: Json) = json.as[ScrollResponse[Json]].getOrElse(e("scroll"))
    def get(json: Json) = json.as[GetResponse[Json]].getOrElse(e("get"))
    def multiGet(json: Json) = ???
    def delete(json: Json) = ???
    def update(json: Json) = ???
  }
}
