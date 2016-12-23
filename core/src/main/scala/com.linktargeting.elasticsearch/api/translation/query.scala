package com.linktargeting.elasticsearch.api.translation

import com.linktargeting.elasticsearch.api._

object query {

  object QueryTranslator extends DataTranslator[Query] {
    def data(query: Query) = {
      val options: DataContainer = query match {
        case QueryWithOptions(_, opts) ⇒ QueryOptionsTranslator.data(opts)
        case _                         ⇒ Map.empty[String, Any]
      }

      val queryData: DataContainer = query match {
        case QueryWithOptions(q, _) ⇒ QueryTranslator.data(q)

        case x: Prefix ⇒ x match {
          case Prefix(field, value, boost) if boost > 0 ⇒ Map("prefix" → Map(field → Map("value" → value.toLowerCase(), "boost" → boost)))
          case Prefix(field, value, _)                  ⇒ Map("prefix" → Map(field -> value.toLowerCase()))
        }

        case Bool(clauses@_*) ⇒ Map("bool" → clauses.flatMap {
          case (Must, queries)    ⇒ Map("must" → queries.map(QueryTranslator.data))
          case (Filter, queries)  ⇒ Map("filter" → queries.map(QueryTranslator.data))
          case (Should, queries)  ⇒ Map("should" → queries.map(QueryTranslator.data))
          case (MustNot, queries) ⇒ Map("must_not" → queries.map(QueryTranslator.data))
        }.toMap)
      }

      queryData ++ options
    }
  }

  private[translation] object QueryOptionsTranslator extends DataTranslator[QueryOptions] {
    override def data(x: QueryOptions) = {
      Map.empty[String, Any] ++ x.size.map("size" → _)
    }
  }
}
