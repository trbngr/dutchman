package com.linktargeting.elasticsearch.api.translation

import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.http._

trait indices {
  private[translation] object IndicesApiTranslator extends DataTranslator[IndicesApi] with RequestTranslator[IndicesApi] {
    def data(api: IndicesApi) = api match {
      case x: DeleteIndex ⇒ DeleteIndexTranslator.data(x)
      case x: Refresh     ⇒ RefreshTranslator.data(x)
    }
    def request(api: IndicesApi) = api match {
      case x: DeleteIndex ⇒ DeleteIndexTranslator.request(x)
      case x: Refresh     ⇒ RefreshTranslator.request(x)
    }
  }

  private[translation] object DeleteIndexTranslator extends DataTranslator[DeleteIndex] with RequestTranslator[DeleteIndex] {
    def data(x: DeleteIndex) = Map.empty
    def request(op: DeleteIndex) = Request(DELETE, s"/${op.index.name}")
  }

  private[translation] object RefreshTranslator extends DataTranslator[Refresh] with RequestTranslator[Refresh] {
    def data(api: Refresh) = Map.empty
    def request(op: Refresh) = {
      Request(POST, op match {
        case Refresh(indices) if indices.isEmpty ⇒ "/_refresh"
        case Refresh(indices)                    ⇒ s"/${indices.map(_.name).mkString(",")}/_refresh"
      })
    }
  }
}
