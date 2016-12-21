package com.linktargeting.elasticsearch.document

import com.linktargeting.elasticsearch._
import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.model._
import org.scalatest.BeforeAndAfterAll

import scala.util.Random

trait BulkSpecs[Json] extends BeforeAndAfterAll {
  this: ApiSpecs[Json] ⇒

  private[this] val idx = Idx("bulk_specs")

  "Bulk" when {
    "updating a non-existing document" should {
      "?" in {
        val action = Bulk(Update(idx, tpe, Person("123", "Frank", "Philly")))
        val json = client.document(Bulk(action)).futureValue
        deleteIndex(idx)
      }
    }

    "index" should {
      "?" in {
        val actions = (1 to 10) map { _ ⇒
          Bulk(Index(idx, tpe, Person(
            id = Random.alphanumeric.take(3).mkString,
            name = Random.alphanumeric.take(3).mkString,
            city = Random.alphanumeric.take(3).mkString
          )))
        }
        val response = client.document(Bulk(actions: _*)).futureValue

        response.size shouldBe 10
        response.map(_.status).toSet should contain only 201

        deleteIndex(idx)
      }
    }
  }
}
