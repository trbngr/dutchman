package dutchman.dsl

trait QueryDsl extends QuerySyntax{

  sealed trait Query

  case class MatchAll(boost: Double = 0) extends Query
  case class Prefix(field: String, value: String, boost: Double = 0) extends Query
  case class Wildcard(field: String, value: String, boost: Double = 0) extends Query
  case class Term(field: String, value: String, boost: Double = 0) extends Query
  case class Ids(ids: Set[Id], `type`: Option[Type] = None) extends Query
  object Ids {
    def apply(ids: Set[Id], `type`: Type): Ids = new Ids(ids, Some(`type`))
  }

  case class Bool(clauses: (BoolQueryClause, Seq[Query])*) extends Query

  sealed trait BoolQueryClause {
    def apply(queries: Query*): (BoolQueryClause, Seq[Query]) = this → queries.toSeq
  }

  case object Must extends BoolQueryClause
  case object Filter extends BoolQueryClause
  case object Should extends BoolQueryClause
  case object MustNot extends BoolQueryClause

}

object QueryDsl extends QueryDsl

