package dutchman

import dutchman.api._
import dutchman.dsl._
import dutchman.marshalling._
import spray.json._

import scala.util.{Failure, Success, Try}

package object sprayjson extends DefaultJsonProtocol {

  private def e(op: String) = throw DecodingError(s"Invalid response: $op")

  private implicit class PimpedJsVaue(json: JsValue) {
    def \(field: String): JsValue = json match {
      case JsObject(obj) ⇒ obj.getOrElse(field, e(s"Can't find field $field"))
      case _ ⇒ e(s"Can't extract field $field from primitive JsValue")
    }

    def as[T: JsonReader](default: T)(implicit reader: JsonReader[T]): T =
      Try(reader.read(json)).getOrElse(default)

    def as[T: JsonReader](implicit reader: JsonReader[T]): T =
      Try(reader.read(json)) match {
        case Success(v) ⇒ v
        case Failure(ex) ⇒ throw DecodingError(s"${ex.getMessage}")
      }
  }

  implicit val IdxFormat: JsonFormat[Idx] = new JsonFormat[Idx] {
    override def read(json: JsValue): Idx = json.convertTo[String]
    override def write(obj: Idx): JsValue = JsString(obj.name)
  }

  implicit val IdFormat: JsonFormat[Id] = new JsonFormat[Id] {
    override def read(json: JsValue): Id = json.convertTo[String]
    override def write(obj: Id): JsValue = JsString(obj.value)
  }

  implicit val TypeFormat: JsonFormat[Type] = new JsonFormat[Type] {
    override def read(json: JsValue): Type = json.convertTo[String]
    override def write(obj: Type): JsValue = JsString(obj.name)
  }

  implicit val ESErrorFormat: JsonFormat[ESError] =
    new JsonFormat[ESError] {
      override def read(json: JsValue): ESError = json match {
        case JsObject(obj) ⇒
          val err = obj.getOrElse("error", e("Can't find field `error`")).asJsObject().fields
          ESError(
            err.get("type").map(_.convertTo[String]).getOrElse(e("Can't find field `type`")),
            err.get("reason").map(_.convertTo[String]).getOrElse(e("Can't find field `reason`")),
            err.get("resource.type").map(_.convertTo[String]).getOrElse(e("Can't find field `resource.type`")),
            err.get("resource.id").map(_.convertTo[Id]).getOrElse(e("Can't find field `resource.id`")),
            err.get("index").map(_.convertTo[Idx]).getOrElse(e("Can't find field `index`")),
            obj.get("status").map(_.convertTo[Int]).getOrElse(0)
          )
        case _ ⇒ e(s"Can't decode object `error`")
      }

      override def write(obj: ESError): JsValue = ???
    }

  implicit val ShardsFormat: RootJsonFormat[Shards] = jsonFormat3(Shards.apply)
  implicit val ResponseFormat: RootJsonFormat[Response] = jsonFormat(Response, "_shards", "_index", "_type", "_id", "_version")
  implicit val IndexResponseFormat: RootJsonFormat[IndexResponse] = jsonFormat2(IndexResponse.apply)
  implicit val DeleteIndexResponseFormat: RootJsonFormat[DeleteIndexResponse] = jsonFormat1(DeleteIndexResponse.apply)

  implicit val RefreshResponseFormat: RootJsonFormat[RefreshResponse] = jsonFormat(RefreshResponse, "_shards")

  implicit val BulkActionFormat = new JsonFormat[BulkAction] {
    override def read(json: JsValue): BulkAction =
      json match {
        case JsString(str) ⇒ str match {
          case "index" ⇒ BulkIndex
          case "create" ⇒ BulkCreate
          case "update" ⇒ BulkUpdate
          case "delete" ⇒ BulkDelete
          case other ⇒ throw new RuntimeException(s"Invalid bulk action: '$other'")
        }

        case _ ⇒ e("wrong json type in BulkAction")
      }

    override def write(obj: BulkAction): JsValue = obj match {
      case BulkIndex ⇒ JsString("index")
      case BulkCreate ⇒ JsString("create")
      case BulkDelete ⇒ JsString("delete")
      case BulkUpdate ⇒ JsString("update")
    }
  }

  implicit val BulkResponseFormat = new JsonFormat[BulkResponse] {
    override def read(json: JsValue): BulkResponse = json match {
      case JsObject(obj) ⇒
        val (actionName, bulkInfo) = obj.head
        val action = BulkActionFormat.read(JsString(actionName))
        val status = IntJsonFormat.read(bulkInfo.asJsObject.getFields("status").head)
        val response = bulkInfo.convertTo[Response]
        BulkResponse(action, status, response)
      case _ ⇒ e("wrong json type in BulkResponse")
    }

    override def write(obj: BulkResponse): JsValue = ???
  }

  implicit def JsonDocumentFormat[T: JsonFormat]: RootJsonFormat[JsonDocument[T]] =
    jsonFormat(JsonDocument.apply[T], "_index", "_type", "_id", "_score", "_source")

  implicit def SearchResponseFormat[T: JsonFormat] =
    new JsonFormat[SearchResponse[T]] {
      override def read(json: JsValue): SearchResponse[T] = {
        val shards: Shards = (json \ "_shards").as[Shards]
        val total: Int = (json \ "hits" \ "total").as[Int](0)
        SearchResponse.apply[T](shards, total, (json \ "hits" \ "hits").as[Seq[JsonDocument[T]]])
      }

      override def write(obj: SearchResponse[T]): JsValue = ???
    }


  implicit object SprayJsonOperationWriter extends ApiDataWriter {
    override def write(data: ApiData): String = if (data.isEmpty) "" else data.toJson

    override def write(data: Seq[ApiData]): String = data map (_.toJson) mkString("", "\n", "\n")
  }

  implicit class RichApiData(data: ApiData) {
    def toJson: String = createJson(data).compactPrint

    private def createJson(param: Any): JsValue = param match {
      case v: String ⇒ JsString(v)
      case v: Boolean ⇒ JsBoolean(v)
      case v: Int ⇒ JsNumber(v)
      case v: Long ⇒ JsNumber(v)
      case v: Float ⇒ JsNumber(v)
      case v: Double ⇒ JsNumber(v)
      case v: BigInt ⇒ JsNumber(v)
      case v: BigDecimal ⇒ JsNumber(v)
      case v: Map[_, _] ⇒ JsObject(v.asInstanceOf[ApiData].map { case (key, value) ⇒ key → createJson(value) }.toSeq: _*)
      case v: Iterable[_] ⇒ JsArray(v.map(createJson).toSeq: _*)
      case null ⇒ JsNull
      case v ⇒ throw new IllegalArgumentException(s"Unsupported scalar value: $v [${v.getClass}]")
    }
  }

  implicit def GetResponseFormat[T: JsonFormat]: RootJsonFormat[GetResponse[T]] =
    jsonFormat(GetResponse.apply[T], "_index", "_type", "_id", "_version", "found", "_source")

  implicit object SprayJsonResponseReader extends ResponseReader[JsValue] {

    def read(json: String): JsValue =
      Try(json.parseJson).getOrElse(JsNull)

    def index(json: JsValue): IndexResponse = {
      IndexResponse(
        response = Try(json.convertTo[Response]).getOrElse(e("index.response")),
        created = json.asJsObject.fields.get("created").exists { case JsBoolean(v) ⇒ v }
      )
    }

    def deleteIndex(json: JsValue): DeleteIndexResponse =
      Try(json.convertTo[DeleteIndexResponse]).getOrElse(e("deleteIndex"))

    def readError(json: JsValue): Option[ESError] = Try(json.convertTo[ESError]).toOption

    def bulk(json: JsValue): Seq[BulkResponse] = (json \ "items").as[Seq[BulkResponse]]

    def search(json: JsValue): SearchResponse[JsValue] = json.as[SearchResponse[JsValue]] //.getOrElse(e("search"))
    def refresh(json: JsValue): RefreshResponse = json.as[RefreshResponse] //.getOrElse(e("refresh"))
    //    def scroll(json: Json) = json.as[ScrollResponse[Json]].getOrElse(e("scroll"))
    def get(json: JsValue): GetResponse[JsValue] = json.as[GetResponse[JsValue]] //.getOrElse(e("get"))
    def multiGet(json: JsValue) = ???

    def delete(json: JsValue) = ???

    def update(json: JsValue) = ???
  }

}
