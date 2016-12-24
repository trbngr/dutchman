package dutchman.api.translation

import dutchman._
import dutchman.api._

object indices {

  object IndicesApiTranslator extends DataTranslator[IndicesApi] with RequestTranslator[IndicesApi] {
    def data(api: IndicesApi) = api match {
      case x: DeleteIndex ⇒ Map.empty
      case x: Refresh     ⇒ Map.empty
    }

    def request(api: IndicesApi) = api match {
      case op: DeleteIndex ⇒ Request(DELETE, s"/${op.index.name}")

      case op: Refresh ⇒ Request(POST, op match {
        case Refresh(indices) if indices.isEmpty ⇒ "/_refresh"
        case Refresh(indices)                    ⇒ s"/${indices.map(_.name).mkString(",")}/_refresh"
      })
    }
  }

}
