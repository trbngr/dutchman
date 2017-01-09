package dutchman.document
import dutchman.ApiSpecs
import dutchman.dsl._
import dutchman.ops._

trait GetSpecs[Json] {
  this: ApiSpecs[Json] ⇒

  "Get" when {
    "Not found" should {
      "be parsed correctly" in {
        val response = client(get[Json]("non_existent", "doc", "234")).futureValue
        response.found shouldBe false
      }
    }
  }

}
