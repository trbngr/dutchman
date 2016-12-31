package dutchman.document

import dutchman.ApiSpecs
import dutchman.api._
import dutchman.model._

trait IndexSpecs[Json] {
  this: ApiSpecs[Json] ⇒

  private[this] val idx: Idx = "index_specs"

  "Index" when {
    val person = Person("123", "Chris", "PHX")

    "it doesn't exist" should {
      "be created" in {
        val ops = client.ops
        val api = for {
          r ← ops.index(idx, tpe, person)
          _ ← ops.deleteIndex(idx)
        } yield r

        val response = client(api).futureValue
        response.created shouldBe true
      }
    }

    "It already exists" should {
      "be not created" in {

        val ops = client.ops
        val api = for {
          _ ← ops.index(idx, tpe, person)
          r ← ops.index(idx, tpe, person)
          _ ← ops.deleteIndex(idx)
        } yield r

        val response = client(api).futureValue
        response.created shouldBe false
      }
    }
  }
}
