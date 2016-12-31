package dutchman.search

import dutchman.api._
import dutchman.model._
import dutchman.{ApiSpecs, ESApi}

import scala.util.Random

trait BoolSpecs[Json] {
  this: ApiSpecs[Json] ⇒

  private[this] val idx: Idx = "bool_specs"

  val boolSpecsIndexActions = (1 to 10) map { i ⇒
    val prefix = if (i % 2 == 0) "even" else "odd"
    BulkAction(
      Index(idx, tpe, Person(
        id = Random.alphanumeric.take(3).mkString,
        name = s"$prefix-${Random.alphanumeric.take(3).mkString}",
        city = s"$prefix-${Random.alphanumeric.take(3).mkString}"
      ))
    )
  }

  "Bool Query" should {
    "Be marshalled" in {
      val query = Bool(
        Must(
          Prefix("name", "ev"),
          Prefix("city", "ev")
        )
      )
      val ops = client.ops

      val api: ESApi[SearchResponse[Json]] = for {
        _ ← ops.bulk(boolSpecsIndexActions: _*)
        r ← ops.search(idx, tpe, query, None)
        _ ← ops.deleteIndex(idx)
      } yield r

      val response = client(api).futureValue
      println(s"RESPONSE: $response")
      response.documents foreach println
    }

    "Be marshalled with multiple should clauses" in {

      val query = Bool(
        Should(
          Prefix("name", "ev"),
          Prefix("city", "ev")
        )
      )

      val ops = client.ops

      val api: ESApi[SearchResponse[Json]] = for {
        _ ← ops.bulk(boolSpecsIndexActions: _*)
        r ← ops.search(idx, tpe, query, None)
        _ ← ops.deleteIndex(idx)
      } yield r

      val response = client(api).futureValue
      println(s"RESPONSE: $response")
      response.documents foreach println

    }
  }

}
