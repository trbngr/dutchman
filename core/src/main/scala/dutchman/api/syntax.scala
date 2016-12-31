package dutchman.api

import dutchman.http._
import dutchman.api.formatting._
import dutchman.marshalling._

trait syntax {

  implicit def stringToId(s: String): Id = Id(s)
  implicit def stringToIdx(s: String): Idx = Idx(s)
  implicit def stringToType(s: String): Type = Type(s)
  implicit def stringsToIndices(s: Seq[String]): Seq[Idx] = s.map(stringToIdx)
  implicit def stringsToTypes(s: Seq[String]): Seq[Type] = s.map(stringToType)

  implicit class RichBulk[A](api: Api[A]) {
    def bulkData: Seq[DataContainer] = apiData(api).get("actions") collect {
      case c: Seq[_] ⇒ c.asInstanceOf[Seq[DataContainer]]
    } getOrElse (throw new RuntimeException("invalid bulk data"))
  }

  implicit class RichApi[A](api: Api[A]) {
    def data: DataContainer = api match {
      case _: Bulk ⇒ throw new UnsupportedOperationException("Use bulkData instead.")
      case _       ⇒ apiData(api)
    }
    def request(implicit writer: OperationWriter): Request = apiRequest(api)
  }
}
