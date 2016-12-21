package com.linktargeting.elasticsearch.api.translation

import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.http._

trait search {

  import query._

  private[translation] object SearchApiTranslator extends DataTranslator[SearchApi] with RequestTranslator[SearchApi] {

    def data(api: SearchApi) = api match {
      case x: Search      ⇒ SearchTranslator.data(x)
      case x: StartScroll ⇒ StartScrollTranslator.data(x)
      case x: Scroll      ⇒ ScrollTranslator.data(x)
      case x: ClearScroll ⇒ ClearScrollTranslator.data(x)
    }
    def request(api: SearchApi) = api match {
      case x: Search      ⇒ SearchTranslator.request(x)
      case x: StartScroll ⇒ StartScrollTranslator.request(x)
      case x: Scroll      ⇒ ScrollTranslator.request(x)
      case x: ClearScroll ⇒ ClearScrollTranslator.request(x)
    }
  }

  private[translation] object SearchTranslator extends DataTranslator[Search] with RequestTranslator[Search] {
    def data(x: Search) = QueryTranslator.data(x.query)
    def request(op: Search) = {
      val path = op match {
        case Search(indices, types, _) if indices.isEmpty && types.isEmpty ⇒ "/_search"
        case Search(indices, types, _) if types.isEmpty                    ⇒ s"/${indices.map(_.name).mkString(",")}/_search"
        case Search(indices, types, _)                                     ⇒ s"/${indices.map(_.name).mkString(",")}/${types.map(_.name).mkString(",")}/_search"
      }
      Request(POST, path)
    }
  }

  private[translation] object StartScrollTranslator extends DataTranslator[StartScroll] with RequestTranslator[StartScroll] {
    def data(o: StartScroll) = QueryTranslator.data(o.query) + ("sort" → Seq("_doc"))
    def request(op: StartScroll) = op match {
      case StartScroll(index, tpe, _, ttl) ⇒
        Request(POST,
          path = s"/${index.name}/${tpe.name}/_search",
          params = Map("scroll" → s"${ttl.toSeconds}s")
        )
    }
  }

  private[translation] object ScrollTranslator extends DataTranslator[Scroll] with RequestTranslator[Scroll] {
    def data(o: Scroll) = Map("sort" → Seq("_doc"))
    def request(op: Scroll) = op match {
      case Scroll(id, ttl) ⇒
        println(s"scroll_id: $id")
        Request(GET,
          path = s"/_search",
          params = Map("scroll_id" → id, "scroll" → s"${ttl.toSeconds}s")
        )
    }
  }

  private[translation] object ClearScrollTranslator extends DataTranslator[ClearScroll] with RequestTranslator[ClearScroll] {
    def data(o: ClearScroll) = Map("scroll_id" → o.scrollIds)
    def request(op: ClearScroll) = Request(DELETE, path = s"/_search/scroll")
  }

}
