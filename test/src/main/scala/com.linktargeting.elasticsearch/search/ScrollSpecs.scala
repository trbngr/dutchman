package com.linktargeting.elasticsearch.search

import com.linktargeting.elasticsearch.ApiSpecs
import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

trait ScrollSpecs[Json] {
  this: ApiSpecs[Json] ⇒

  private[this] val idx: Idx = "scroll_specs"

  def indexPeople = {
    val actions = (1 to 10) map { i ⇒
      Bulk(
        Index(idx, tpe, Person(
          id = Random.alphanumeric.take(3).mkString,
          name = s"${if (i % 2 == 0) "even" else "odd"}-${Random.alphanumeric.take(3).mkString}",
          city = Random.alphanumeric.take(3).mkString
        ))
      )
    }

    dsl.document(Bulk(actions: _*)) map { _ ⇒
      refresh(idx)
    }
  }

  "Scroll" should {
    "work" in {
      whenReady(indexPeople) { _ ⇒
        var ids = Set.empty[String]

        try {
          val query = Prefix("name", "even")

          val start = StartScroll(idx, tpe, query).task.futureValue
          ids = ids + start.scrollId
          start.scrollId shouldNot be(empty)
          start.results.documents foreach println
          start.results.total shouldBe 10

          val persons = start.results.documents.map(x ⇒ readPerson(x.source))
          persons.size shouldBe 10

          if (persons.size != start.results.total) {
            //next page
            val page = Scroll(start.scrollId).task.futureValue

            ids = ids + page.scrollId
            page.scrollId shouldNot be(empty)
            page.results.total shouldBe 10
            val nextPersons = page.results.documents.map(x ⇒ readPerson(x.source))
            nextPersons.size shouldBe 5
          }
        } finally {
          ClearScroll(ids)().futureValue

          deleteIndex(idx)
        }
      }
    }
  }
}
