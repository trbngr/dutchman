package dutchman.api.formatting

import dutchman.api._
import dutchman.http._

object indices {

  private[formatting] object IndicesApiFormatter extends DataFormatter[IndicesApi] with RequestFormatter[IndicesApi] {
    def data(api: IndicesApi) = api match {
      case _: DeleteIndex ⇒ Map.empty
      case _: Refresh     ⇒ Map.empty
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
