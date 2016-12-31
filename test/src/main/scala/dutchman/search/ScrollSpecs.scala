package dutchman.search

import dutchman.api._
import dutchman.model._
import dutchman.{ApiSpecs, ESApi}

import scala.util.Random

trait ScrollSpecs[Json] {
  this: ApiSpecs[Json] ⇒

  private[this] val idx: Idx = "scroll_specs"

  val scrollSpecsIndexActions = (1 to 10) map { i ⇒
    Bulk(
      Index(idx, tpe, Person(
        id = Random.alphanumeric.take(3).mkString,
        name = s"${if (i % 2 == 0) "even" else "odd"}-${Random.alphanumeric.take(3).mkString}",
        city = Random.alphanumeric.take(3).mkString
      ))
    )
  }

  "Scroll" should {
    "work" in {
      var ids = Set.empty[String]

      val ops = client.ops

      val api: ESApi[ScrollResponse[Json]] = for {
        _ ← ops.bulk(scrollSpecsIndexActions: _*)
        _ ← ops.refresh(idx)
        r ← ops.startScroll(idx, tpe, Prefix("name", "even"), options = None)
        _ ← ops.deleteIndex(idx)
      } yield r

      val start = client(api).futureValue
      ids = ids + start.scrollId
      start.scrollId shouldNot be(empty)
      start.results.documents foreach println
      start.results.total shouldBe 10

      val persons = start.results.documents.map(x ⇒ readPerson(x.source))
      persons.size shouldBe 10
    }
  }
}
