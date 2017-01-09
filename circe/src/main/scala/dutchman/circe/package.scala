package dutchman

import cats.syntax.either._
import dutchman.api._
import dutchman.dsl._
import dutchman.marshalling._
import io.circe._
import io.circe.parser._

package object circe {

  implicit object CirceOperationWriter extends ApiDataWriter {
    override def write(data: ApiData) = if (data.isEmpty) "" else data.toJson
    override def write(data: Seq[ApiData]) = data map (_.toJson) mkString("", "\n", "\n")
  }

  implicit class RichApiData(data: ApiData) {
    def toJson = createJson(data).noSpaces

    private def createJson(value: Any): Json = value match {
      case v: String      ⇒ Json.fromString(v)
      case v: Boolean     ⇒ Json.fromBoolean(v)
      case v: Int         ⇒ Json.fromInt(v)
      case v: Long        ⇒ Json.fromLong(v)
      case v: Float       ⇒ Json.fromDoubleOrNull(v)
      case v: Double      ⇒ Json.fromDoubleOrNull(v)
      case v: BigInt      ⇒ Json.fromBigInt(v)
      case v: BigDecimal  ⇒ Json.fromBigDecimal(v)
      case v: Map[_, _]   ⇒ Json.obj(v.asInstanceOf[ApiData].map(x ⇒ x._1 → createJson(x._2)).toSeq: _*)
      case v: Iterable[_] ⇒ Json.arr(v.map(createJson).toSeq: _*)
      case null           ⇒ Json.Null
      case v              ⇒ throw new IllegalArgumentException(s"Unsupported scalar value: $v [${v.getClass}]")
    }
  }

  implicit object CirceResponseReader extends ResponseReader[Json] {

    import codecs._
    import syntax._

    private def e(op: String) = throw DecodingError(s"Invalid response: $op")

    def read(json: String) = parse(json).getOrElse(Json.Null)

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
      //      val errors = json \ "errors" bool false
      None
    }
    def bulk(json: Json) = json \ "items" bulkResponses
    def search(json: Json) = json.as[SearchResponse[Json]].getOrElse(e("search"))
    def refresh(json: Json) = json.as[RefreshResponse].getOrElse(e("refresh"))
    //    def scroll(json: Json) = json.as[ScrollResponse[Json]].getOrElse(e("scroll"))
    def get(json: Json) = json.as[GetResponse[Json]].getOrElse(e("get"))
    def multiGet(json: Json) = ???
    def delete(json: Json) = ???
    def update(json: Json) = ???
  }
}
