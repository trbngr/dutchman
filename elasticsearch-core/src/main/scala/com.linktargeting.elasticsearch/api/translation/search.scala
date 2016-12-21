package com.linktargeting.elasticsearch.api.translation

import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.http._

object search {

  import query.QueryTranslator

  object SearchApiTranslator extends DataTranslator[SearchApi] with RequestTranslator[SearchApi] {

    def data(api: SearchApi) = api match {
      case x: Search      ⇒ QueryTranslator.data(x.query)
      case x: StartScroll ⇒ QueryTranslator.data(x.query) + ("sort" → Seq("_doc"))
      case x: Scroll      ⇒ Map("sort" → Seq("_doc"))
      case x: ClearScroll ⇒ Map("scroll_id" → x.scrollIds)
    }

    def request(api: SearchApi) = api match {
      case x: Search      ⇒
        val path = x match {
          case Search(indices, types, _) if indices.isEmpty && types.isEmpty ⇒ "/_search"
          case Search(indices, types, _) if types.isEmpty                    ⇒ s"/${indices.map(_.name).mkString(",")}/_search"
          case Search(indices, types, _)                                     ⇒ s"/${indices.map(_.name).mkString(",")}/${types.map(_.name).mkString(",")}/_search"
        }
        Request(POST, path)
      case x: StartScroll ⇒ x match {
        case StartScroll(index, tpe, _, ttl) ⇒
          Request(POST,
            path = s"/${index.name}/${tpe.name}/_search",
            params = Map("scroll" → s"${ttl.toSeconds}s")
          )
      }
      case x: Scroll      ⇒ x match {
        case Scroll(id, ttl) ⇒
          println(s"scroll_id: $id")
          Request(GET,
            path = s"/_search",
            params = Map("scroll_id" → id, "scroll" → s"${ttl.toSeconds}s")
          )
      }
      case x: ClearScroll ⇒ Request(DELETE, path = s"/_search/scroll")
    }
  }

}
