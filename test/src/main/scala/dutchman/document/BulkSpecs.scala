package dutchman.document

import dutchman.dsl._
import dutchman.ops._
import dutchman.model._
import dutchman._
import org.scalatest.BeforeAndAfterAll

import scala.util.Random

trait BulkSpecs[Json] extends BeforeAndAfterAll {
  this: ApiSpecs[Json] ⇒

  private[this] val idx: Idx = "bulk_specs"
  private[this] val tpe: Type = "person"

  "Bulk" when {
    "updating a non-existing document" should {
      "?" in {
        val action = BulkAction(Update(idx, tpe, Person("123", "Frank", "Philly")))

        val api = for {
          r ← bulk(Seq(action): _*)
          _ ← deleteIndex(idx)
        } yield r

        val response = client(api).futureValue
        println(response)
      }
    }

    "index" should {
      "?" in {
        val actions = (1 to 10) map { _ ⇒
          val document = Person(
            id = Random.alphanumeric.take(3).mkString,
            name = Random.alphanumeric.take(3).mkString,
            city = Random.alphanumeric.take(3).mkString
          )
          BulkAction(Index(idx, tpe, document, None))
        }

        val api = for {
          r ← bulk(actions: _*)
          _ ← deleteIndex(idx)
        } yield r

        val response = client(api).futureValue
        println(response)
      }
    }
  }
}
