package dutchman.document

import dutchman.api._
import dutchman.model._
import dutchman._
import org.scalatest.BeforeAndAfterAll

import scala.util.Random

trait BulkSpecs[Json] extends BeforeAndAfterAll {
  this: ApiSpecs[Json] ⇒

  private[this] val idx: Idx = "bulk_specs"

  "Bulk" when {
    "updating a non-existing document" should {
      "?" in {
        val action = BulkAction(Update(idx, tpe, Person("123", "Frank", "Philly")))
        val ops = client.ops
        val api = for {
          r ← ops.bulk(Seq(action): _*)
          _ ← ops.deleteIndex(idx)
        } yield r

        val response = client(api).futureValue
        println(response)
      }
    }

    "index" should {
      "?" in {
        val actions = (1 to 10) map { _ ⇒
          BulkAction(Index(idx, tpe, Person(
            id = Random.alphanumeric.take(3).mkString,
            name = Random.alphanumeric.take(3).mkString,
            city = Random.alphanumeric.take(3).mkString
          )))
        }

        val ops = client.ops
        val api = for {
          r ← ops.bulk(actions: _*)
          _ ← ops.deleteIndex(idx)
        } yield r

        val response = client(api).futureValue
        println(response)
      }
    }
  }
}
