package dutchman.api

import dutchman.Request

trait syntax {
  implicit def stringToId(s: String): Id = Id(s)
  implicit def stringToIdx(s: String): Idx = Idx(s)
  implicit def stringToType(s: String): Type = Type(s)
  implicit def stringsToIndices(s: Seq[String]): Seq[Idx] = s.map(stringToIdx)
  implicit def stringsToTypes(s: Seq[String]): Seq[Type] = s.map(stringToType)

  implicit class RichApi(api: Api) {

    import dutchman.api.translation.{apiData, apiRequest}
    import dutchman.marshalling._

    def data: DataContainer = apiData(api)
    def request(implicit marshaller: ApiMarshaller): Request = apiRequest(api)
  }

}
