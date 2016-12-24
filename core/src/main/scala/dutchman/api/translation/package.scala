package dutchman.api

import dutchman._
import dutchman.marshalling.ApiMarshaller

package object translation {

  import document.DocumentApiTranslator
  import indices.IndicesApiTranslator
  import search.SearchApiTranslator

  type DataContainer = Map[String, Any]
  type BulkContainers = Seq[DataContainer]

  def apiData(api: Api): DataContainer = api match {
    case v: DocumentApi ⇒ DocumentApiTranslator.data(v)
    case v: IndicesApi  ⇒ IndicesApiTranslator.data(v)
    case v: SearchApi   ⇒ SearchApiTranslator.data(v)
  }

  def apiRequest(api: Api)(implicit marshaller: ApiMarshaller): Request = {
    val request = api match {
      case v: DocumentApi ⇒ DocumentApiTranslator.request(v)
      case v: IndicesApi  ⇒ IndicesApiTranslator.request(v)
      case v: SearchApi   ⇒ SearchApiTranslator.request(v)
    }
    request.copy(
      payload = marshaller.marshal(api)
    )
  }

  trait DataTranslator[A] {
    def data(o: A): DataContainer
  }

  trait RequestTranslator[A <: Api] {
    def request(op: A): Request
  }


}
