package dutchman.document

import dutchman.ApiSpecs
import dutchman.dsl._
import dutchman.model._
import dutchman.ops._

trait IndexSpecs[Json] {
  this: ApiSpecs[Json] ⇒

  private[this] val idx: Idx = "index_specs"
  private[this] val tpe: Type = "person"

  "Index" when {
    val person = Person("123", "Chris", "PHX")

    "it doesn't exist" should {
      "be created" in {
        val api = for {
          r ← index(idx, tpe, person, None)
          _ ← deleteIndex(idx)
        } yield r

        val response = client(api).futureValue
        response.created shouldBe true
      }
    }

    "It already exists" should {
      "be not created" in {

        val api = for {
          _ ← index(idx, tpe, person, None)
          r ← index(idx, tpe, person, None)
          _ ← deleteIndex(idx)
        } yield r

        val response = client(api).futureValue
        response.created shouldBe false
      }
    }
  }
}
