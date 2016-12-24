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

        case x: Wildcard ⇒ x match {
          case Wildcard(field, value, boost) if boost > 0 ⇒ Map("wildcard" → Map(field → Map("value" → value.toLowerCase(), "boost" → boost)))
          case Wildcard(field, value, _)                  ⇒ Map("wildcard" → Map(field -> value.toLowerCase()))
        }

        case x: Term ⇒ x match {
          case Term(field, value, boost) if boost > 0 ⇒ Map("term" → Map(field → Map("value" → value.toLowerCase(), "boost" → boost)))
          case Term(field, value, _)                  ⇒ Map("term" → Map(field -> value.toLowerCase()))
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
