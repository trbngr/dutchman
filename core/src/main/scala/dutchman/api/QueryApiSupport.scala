package dutchman.api

trait QueryApiSupport {

  import dutchman.dsl._

  implicit class QueryData(query: Query) {
    def data: ApiData = query match {

      case x: MatchAll ⇒ x match {
        case MatchAll(boost) if boost > 0 ⇒ Map("match_all" → Map("boost" → boost))
        case MatchAll(_)                  ⇒ Map("match_all" → Map())
      }

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
        case (Must, queries)    ⇒ Map("must" → queries.map(_.data))
        case (Filter, queries)  ⇒ Map("filter" → queries.map(_.data))
        case (Should, queries)  ⇒ Map("should" → queries.map(_.data))
        case (MustNot, queries) ⇒ Map("must_not" → queries.map(_.data))
      }.toMap)

      case Ids(ids, tpe) ⇒ Map("ids" → Map("values" → ids.map(_.value))) ++ tpe.map("type" → _)
    }

  }
}
