package dutchman

import cats.syntax.either._
import dutchman.api._
import dutchman.dsl._
import dutchman.marshalling._
import io.circe._
import io.circe.parser._

package object circe {

  implicit object CirceOperationWriter extends ApiDataWriter {
    override def write(data: ApiData): String = if (data.isEmpty) "" else data.toJson
    override def write(data: Seq[ApiData]): String = data map (_.toJson) mkString("", "\n", "\n")
  }

  implicit class RichApiData(data: ApiData) {
    def toJson: String = createJson(data).noSpaces

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

    def read(json: String): Json = parse(json).getOrElse(Json.Null)

    def index(json: Json): IndexResponse = {
      IndexResponse(
        response = json.as[Response].getOrElse(e("index.response")),
        created = json \ "created" bool false
      )
    }
    def deleteIndex(json: Json): DeleteIndexResponse = json.as[DeleteIndexResponse].getOrElse(e("deleteIndex"))

    def readError(json: Json): Option[ESError] = {
      json.cursor.downField("error") match {
        case None ⇒ None
        case _    ⇒ json.as[ESError] match {
          case Right(e) ⇒ Some(e)
          case Left(_)  ⇒ None
        }
      }
    }
    def bulk(json: Json): Seq[BulkResponse] = json \ "items" bulkResponses
    def search(json: Json): SearchResponse[Json] = json.as[SearchResponse[Json]].getOrElse(e("search"))
    def refresh(json: Json): RefreshResponse = json.as[RefreshResponse].getOrElse(e("refresh"))
    //    def scroll(json: Json) = json.as[ScrollResponse[Json]].getOrElse(e("scroll"))
    def get(json: Json): GetResponse[Json] = json.as[GetResponse[Json]].getOrElse(e("get"))
    def multiGet(json: Json) = ???
    def delete(json: Json) = ???
    def update(json: Json) = ???
  }
}
