package com.linktargeting.elasticsearch.api.translation

import com.linktargeting.elasticsearch.api._

object query {

  object QueryTranslator extends DataTranslator[Query] {
    def data(query: Query) = {
      val options = query match {
        case x: QueryWithOptions ⇒ Map.empty ++ x.options.size.map("size" → _)
        case _                   ⇒ Map.empty[String, Any]
      }
      val queryData = Map("query" → (query match {
        case x: PrefixQuery ⇒ PrefixQueryTranslator.data(x)
      }))
      Map(queryData.toSeq ++ options.toSeq: _*)
    }
  }

  private[translation] object PrefixQueryTranslator extends DataTranslator[PrefixQuery] {
    override def data(x: PrefixQuery) = x match {
      case PrefixQuery(field, value, boost, _) if boost > 0 ⇒ Map("prefix" → Map(field → Map("value" → value, "boost" → boost)))
      case PrefixQuery(field, value, _, _)                  ⇒ Map("prefix" → Map(field -> value))
    }
  }
}
