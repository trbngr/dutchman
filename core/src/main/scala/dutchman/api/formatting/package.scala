package dutchman.api

import dutchman.api.formatting.document.DocumentApiFormatter
import dutchman.api.formatting.search.SearchApiFormatter
import dutchman.http.Request
import dutchman.marshalling.ApiMarshaller

package object formatting {

  type DataContainer = Map[String, Any]

  import dutchman.api.formatting._
  import indices.IndicesApiFormatter

  private[dutchman] def apiData[A](api: Api[A]): DataContainer = api match {
    case v: DocumentApi ⇒ DocumentApiFormatter.data(v)
    case v: IndicesApi  ⇒ IndicesApiFormatter.data(v)
    case v: SearchApi   ⇒ SearchApiFormatter.data(v)
  }

  private[dutchman] def apiRequest[A](api: Api[A])(implicit marshaller: ApiMarshaller): Request = {
    val request = api match {
      case v: DocumentApi ⇒ DocumentApiFormatter.request(v)
      case v: IndicesApi  ⇒ IndicesApiFormatter.request(v)
      case v: SearchApi   ⇒ SearchApiFormatter.request(v)
    }
    request.copy(
      payload = marshaller.marshal(api)
    )
  }

  trait DataFormatter[A] {
    def data(o: A): DataContainer
  }

  trait RequestFormatter[A] {
    def request(op: A): Request
  }
}
