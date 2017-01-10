package dutchman.document
import dutchman.ApiSpecs
import dutchman.dsl._
import dutchman.ops._

trait GetSpecs[Json] {
  this: ApiSpecs[Json] ⇒

  "Get" when {
    "index doesn't exist" should {
      "return error" in {
        val response = client(get[Json]("non_existent", "doc", "234")).futureValue
        println(response)
        response match {
          case Left(e) ⇒ ()
          case Right(d) ⇒ fail("should have returned error")
        }
      }
    }

    "index exists but document doesn't" should {
      "return found = false" in {
        val prog = for {
          _ ← index("test_index", "test", ElasticDocument(2, Map("id" → 2)), None)
          r ← get[Json]("test_index", "test", "234")
        } yield  r

        val response = client(prog).futureValue
        println(response)
        response match {
          case Left(e) ⇒ fail(s"should have returned document not found. instead got: ${e.reason}")
          case Right(v) ⇒ v.found shouldBe false
        }
      }
    }
  }

}
