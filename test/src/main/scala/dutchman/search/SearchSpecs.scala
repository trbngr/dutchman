package dutchman.search

import dutchman.ApiSpecs
import dutchman.api._
import dutchman.model._

import scala.util.Random

trait SearchSpecs[Json] {
  this: ApiSpecs[Json] ⇒

  private[this] val idx: Idx = "search_specs"

  val searchSpecsIndexActions = (1 to 10) map { i ⇒
    val person = Person(
      id = Random.alphanumeric.take(3).mkString,
      name = s"${if (i % 2 == 0) "even" else "odd"}-${Random.alphanumeric.take(3).mkString}",
      city = Random.alphanumeric.take(3).mkString
    )

    Bulk(Index(idx, tpe, person))
  }

  "PrefixQuery" when {
    "run" should {
      "work" in {
        val opts = SearchOptions(size = Some(25))
        val ops = client.ops

        val api = for {
          _ ← ops.bulk(searchSpecsIndexActions: _*)
          _ ← ops.refresh(idx)
          r ← ops.search(idx, tpe, Prefix("name", "even"), Some(opts))
          _ ← ops.deleteIndex(idx)
        } yield r

        val response = client(api).futureValue
        response.total shouldBe 5

        val persons = response.documents.map(x ⇒ readPerson(x.source))
        persons.size shouldBe 5

      }
    }
  }
}
