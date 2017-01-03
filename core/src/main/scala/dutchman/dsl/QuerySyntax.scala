package dutchman.dsl

import scala.util.{Success, Try}

trait QuerySyntax {

  case object InvalidQueryPatternError extends Exception("Invalid query pattern")

  implicit class QueryInterpolation(val sc: StringContext) {

    def extract[T](input: String)(factory: PartialFunction[(String, String, Double), T]): T = {
      val pattern = "([^:]+):([^:]+):?([0-9]*)?".r
      input match {
        case pattern(field, value, boostStr) ⇒
          val boost = Try(boostStr.toDouble) match {
            case Success(b) ⇒ b
            case _          ⇒ 0
          }
          factory((field, value, boost))
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
