package dutchman.api.translation

import dutchman.api._

object query {

  object QueryTranslator extends DataTranslator[Query] {
    def data(query: Query) = {
      query match {
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
    }
  }
}
