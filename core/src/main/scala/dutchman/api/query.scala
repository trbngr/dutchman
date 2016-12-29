package dutchman.api

import scala.util.{Success, Try}

trait query {

  sealed trait Query

  sealed trait BoolQueryClause {
    def apply(queries: Query*): (BoolQueryClause, Seq[Query]) = this → queries.toSeq
  }

  case class QueryWithOptions(query: Query, options: SearchOptions) extends Query

  case class MatchAll(boost: Double = 0) extends Query
  case class Prefix(field: String, value: String, boost: Double = 0) extends Query
  case class Wildcard(field: String, value: String, boost: Double = 0) extends Query
  case class Term(field: String, value: String, boost: Double = 0) extends Query

  case class Bool(clauses: (BoolQueryClause, Seq[Query])*) extends Query
  case object Must extends BoolQueryClause
  case object Filter extends BoolQueryClause
  case object Should extends BoolQueryClause
  case object MustNot extends BoolQueryClause

  case object InvalidQueryPatternError extends Exception("Invalid query pattern")

  implicit class QueryInterpolation(val sc: StringContext) {

    def extract[T](input: String)(factory: PartialFunction[(String, String, Double), T]): T = {
      val pattern = "([^:]+):([^:]+):?([0-9]*)?".r
      input match {
        case pattern(field, value, boostStr) ⇒ factory((field, value, Try(boostStr.toDouble) match {
          case Success(boost) ⇒ boost
          case _              ⇒ 0
        }))
        case _                               ⇒ throw InvalidQueryPatternError
      }
    }

    def prefix(args: Any*): Prefix = extract(sc.s(args: _*)) {
      case (field, value, boost) ⇒ Prefix(field, value, boost)
    }

    def wildcard(args: Any*): Wildcard = extract(sc.s(args: _*)) {
      case (field, value, boost) ⇒ Wildcard(field, value, boost)
    }

    def term(args: Any*): Term = extract(sc.s(args: _*)) {
      case (field, value, boost) ⇒ Term(field, value, boost)
    }
  }
}
