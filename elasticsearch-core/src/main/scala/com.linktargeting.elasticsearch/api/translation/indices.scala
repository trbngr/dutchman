package com.linktargeting.elasticsearch.api.translation

import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.http._

trait indices {
  private[translation] object IndicesApiMapper extends DataMapper[IndicesApi] with HttpBuilder[IndicesApi] {
    def data(api: IndicesApi) = api match {
      case x: DeleteIndex ⇒ DeleteIndexMapper.data(x)
      case x: Refresh     ⇒ RefreshMapper.data(x)
    }
    def buildRequest(api: IndicesApi) = api match {
      case x: DeleteIndex ⇒ DeleteIndexMapper.buildRequest(x)
      case x: Refresh     ⇒ RefreshMapper.buildRequest(x)
    }
  }

  private[translation] object DeleteIndexMapper extends DataMapper[DeleteIndex] with HttpBuilder[DeleteIndex] {
    def data(x: DeleteIndex) = Map.empty
    def buildRequest(op: DeleteIndex) = Request(DELETE, s"/${op.index.name}")
  }

  private[translation] object RefreshMapper extends DataMapper[Refresh] with HttpBuilder[Refresh] {
    def data(api: Refresh) = Map.empty
    def buildRequest(op: Refresh) = {
      Request(POST, op match {
        case Refresh(indices) if indices.isEmpty ⇒ "/_refresh"
        case Refresh(indices)                    ⇒ s"/${indices.map(_.name).mkString(",")}/_refresh"
      })
    }
  }
}
