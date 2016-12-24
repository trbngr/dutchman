package dutchman.search

import dutchman.ApiSpecs
import dutchman.api._
import dutchman.dsl._
import dutchman.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

trait SearchSpecs[Json] {
  this: ApiSpecs[Json] ⇒

  private[this] val idx: Idx = "search_specs"

  def indexPeople = {
    val actions = (1 to 10) map { i ⇒
      val person = Person(
        id = Random.alphanumeric.take(3).mkString,
        name = s"${if (i % 2 == 0) "even" else "odd"}-${Random.alphanumeric.take(3).mkString}",
        city = Random.alphanumeric.take(3).mkString
      )

      Bulk(Index(idx, tpe, person))
    }

    for {
      _ ← Bulk(actions: _*)
      _ ← Refresh(idx)
    } yield ()
  }

  "PrefixQuery" when {
    "run" should {
      "work" in {
        whenReady(indexPeople) { _ ⇒
          try {
            val opts = SearchOptions(size = Some(25))

            Search(idx, tpe, Prefix("name", "even")).withOptions(opts) map { response ⇒
              response.total shouldBe 5

              val persons = response.documents.map(x ⇒ readPerson(x.source))
              persons.size shouldBe 5
            } futureValue
          } finally deleteIndex(idx)
        }
      }
    }
  }
}
