package dutchman.search

import dutchman._
import dutchman.dsl._
import dutchman.ops._
import dutchman.model._

import scala.util.Random

trait ScrollSpecs[Json] {
  this: ApiSpecs[Json] ⇒

//  private[this] val idx: Idx = "scroll_specs"
//
//  val scrollSpecsIndexActions = (1 to 10) map { i ⇒
//    val document = Person(
//      id = Random.alphanumeric.take(3).mkString,
//      name = s"${if (i % 2 == 0) "even" else "odd"}-${Random.alphanumeric.take(3).mkString}",
//      city = Random.alphanumeric.take(3).mkString
//    )
//    BulkAction(Index(idx, tpe, document, None))
//  }
//
//  "Scroll" should {
//    "work" in {
//      var ids = Set.empty[String]
//
//      val api = for {
//        _ ← bulk(scrollSpecsIndexActions: _*)
//        _ ← refresh(idx)
//        r ← startScroll(idx, tpe, Prefix("name", "even"), options = None)
//        _ ← deleteIndex(idx)
//      } yield r
//
//      val start = client(api).futureValue
//      ids = ids + start.scrollId
//      start.scrollId shouldNot be(empty)
//      start.results.documents foreach println
//      start.results.total shouldBe 10
//
//      val persons = start.results.documents.map(x ⇒ readPerson(x.source))
//      persons.size shouldBe 10
//    }
//  }
}
