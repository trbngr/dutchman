package com.linktargeting.elasticsearch.search

import com.linktargeting.elasticsearch.ApiSpecs
import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.model._

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

    dsl.document(Bulk(actions: _*)) map { _ ⇒
      refresh(idx)
    }
  }

  "PrefixQuery" when {
    "run" should {
      "work" in {
        whenReady(indexPeople) { _ ⇒
          try {
            Search(idx, tpe, Prefix("name", "even")) map { response ⇒
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
