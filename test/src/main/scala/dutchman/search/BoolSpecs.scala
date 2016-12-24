package dutchman.search

import dutchman.ApiSpecs
import dutchman.api._
import dutchman.dsl._
import dutchman.model._

import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.Random

trait BoolSpecs[Json] {
  this: ApiSpecs[Json] ⇒

  private[this] val idx: Idx = "bool_specs"

  def indexPeople = {
    val actions = (1 to 10) map { i ⇒
      val prefix = if (i % 2 == 0) "even" else "odd"
      Bulk(
        Index(idx, tpe, Person(
          id = Random.alphanumeric.take(3).mkString,
          name = s"$prefix-${Random.alphanumeric.take(3).mkString}",
          city = s"$prefix-${Random.alphanumeric.take(3).mkString}"
        ))
      )
    }

    dsl.document(Bulk(actions: _*)) map { _ ⇒
      refresh(idx)
    }
  }

  "Bool Query" should {
    "Be marshalled" in {
      try {
        whenReady(indexPeople) { _ ⇒
          val query = Bool(
            Must(Prefix("name", "ev"), Prefix("city", "ev"))
          )
          Search(idx, tpe, query).map { r ⇒
            println(s"RESPONSE: $r")
            r.documents foreach println
          }
        }
      } finally {
        deleteIndex(idx)
      }
    }
  }

}
