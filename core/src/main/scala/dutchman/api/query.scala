package dutchman.api

trait query {

  sealed trait Query

  sealed trait BoolQueryClause {
    def apply(queries: Query*): (BoolQueryClause, Seq[Query]) = this → queries.toSeq
  }

  case class QueryWithOptions(query: Query, options: SearchOptions) extends Query

  case class Prefix(field: String, value: String, boost: Float = 0) extends Query
  case class Wildcard(field: String, value: String, boost: Float = 0) extends Query
  case class Term(field: String, value: String, boost: Float = 0) extends Query

  case class Bool(clauses: (BoolQueryClause, Seq[Query])*) extends Query
  case object Must extends BoolQueryClause
  case object Filter extends BoolQueryClause
  case object Should extends BoolQueryClause
  case object MustNot extends BoolQueryClause
}
