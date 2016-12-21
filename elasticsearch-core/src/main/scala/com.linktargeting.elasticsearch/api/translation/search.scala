package com.linktargeting.elasticsearch.api.translation

import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.http._

trait search {

  import query._

  private[translation] object SearchApiMapper extends DataMapper[SearchApi] with HttpBuilder[SearchApi] {

    def data(api: SearchApi) = api match {
      case x: Search      ⇒ SearchMapper.data(x)
      case x: StartScroll ⇒ StartScrollMapper.data(x)
      case x: Scroll      ⇒ ScrollMapper.data(x)
      case x: ClearScroll ⇒ ClearScrollMapper.data(x)
    }
    def buildRequest(api: SearchApi) = api match {
      case x: Search      ⇒ SearchMapper.buildRequest(x)
      case x: StartScroll ⇒ StartScrollMapper.buildRequest(x)
      case x: Scroll      ⇒ ScrollMapper.buildRequest(x)
      case x: ClearScroll ⇒ ClearScrollMapper.buildRequest(x)
    }
  }

  private[translation] object SearchMapper extends DataMapper[Search] with HttpBuilder[Search] {
    def data(x: Search) = QueryMapper.data(x.query)
    def buildRequest(op: Search) = {
      val path = op match {
        case Search(indices, types, _) if indices.isEmpty && types.isEmpty ⇒ "/_search"
        case Search(indices, types, _) if types.isEmpty                    ⇒ s"/${indices.map(_.name).mkString(",")}/_search"
        case Search(indices, types, _)                                     ⇒ s"/${indices.map(_.name).mkString(",")}/${types.map(_.name).mkString(",")}/_search"
      }
      Request(POST, path)
    }
  }

  private[translation] object StartScrollMapper extends DataMapper[StartScroll] with HttpBuilder[StartScroll] {
    def data(o: StartScroll) = QueryMapper.data(o.query) + ("sort" → Seq("_doc"))
    def buildRequest(op: StartScroll) = op match {
      case StartScroll(index, tpe, _, ttl) ⇒
        Request(POST,
          path = s"/${index.name}/${tpe.name}/_search",
          params = Map("scroll" → s"${ttl.toSeconds}s")
        )
    }
  }

  private[translation] object ScrollMapper extends DataMapper[Scroll] with HttpBuilder[Scroll] {
    def data(o: Scroll) = Map("sort" → Seq("_doc"))
    def buildRequest(op: Scroll) = op match {
      case Scroll(id, ttl) ⇒
        println(s"scroll_id: $id")
        Request(GET,
          path = s"/_search",
          params = Map("scroll_id" → id, "scroll" → s"${ttl.toSeconds}s")
        )
    }
  }

  private[translation] object ClearScrollMapper extends DataMapper[ClearScroll] with HttpBuilder[ClearScroll] {
    def data(o: ClearScroll) = Map("scroll_id" → o.scrollIds)
    def buildRequest(op: ClearScroll) = Request(DELETE, path = s"/_search/scroll")
  }

}
