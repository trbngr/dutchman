package dutchman.search

import dutchman.ApiSpecs
import dutchman.dsl._
import dutchman.model._
import dutchman.ops._

import scala.util.Random

trait SearchSpecs[Json] {
  this: ApiSpecs[Json] ⇒

  private[this] val idx: Idx = "search_specs"
  private[this] val tpe: Type = "person"

  val searchSpecsIndexActions = (1 to 10) map { i ⇒
    val document = Person(
      id = Random.alphanumeric.take(3).mkString,
      name = s"${if (i % 2 == 0) "even" else "odd"}-${Random.alphanumeric.take(3).mkString}",
      city = Random.alphanumeric.take(3).mkString
    )

    BulkAction(Index(idx, tpe, document, None))
  }

  "PrefixQuery" when {
    "run" should {
      "work" in {
        val opts = SearchOptions(size = Some(25))

        val api = for {
          _ ← bulk(searchSpecsIndexActions: _*)
          _ ← refresh(idx)
          r ← search[Json](idx, tpe, Prefix("name", "even"), Some(opts))
          _ ← deleteIndex(idx)
        } yield r

        val response = client(api).futureValue
        response.total shouldBe 5

        val persons = response.documents.map(x ⇒ readPerson(x.source))
        persons.size shouldBe 5

      }
    }
  }
}
