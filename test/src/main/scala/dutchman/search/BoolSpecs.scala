package dutchman.search

import dutchman.{ApiSpecs, ElasticOps}
import dutchman.dsl._
import dutchman.ops._
import dutchman.model._

import scala.util.Random

trait BoolSpecs[Json] {
  this: ApiSpecs[Json] ⇒

  private[this] val idx: Idx = "bool_specs"
  private[this] val tpe: Type = "person"

  val boolSpecsIndexActions = (1 to 10) map { i ⇒
    val prefix = if (i % 2 == 0) "even" else "odd"
    val document = Person(
      id = Random.alphanumeric.take(3).mkString,
      name = s"$prefix-${Random.alphanumeric.take(3).mkString}",
      city = s"$prefix-${Random.alphanumeric.take(3).mkString}"
    )

    BulkAction(Index(idx, tpe, document, None))
  }

  "Bool Query" should {
    "Be marshalled" in {
      val query = Bool(
        Must(
          Prefix("name", "ev"),
          Prefix("city", "ev")
        )
      )

      val api = for {
        _ ← bulk(boolSpecsIndexActions: _*)
        r ← search[Json](idx, tpe, query, None)
        _ ← deleteIndex(idx)
      } yield r

      val response = client(api).futureValue
      println(s"RESPONSE: $response")
      response match {
        case Right(v) ⇒ v.documents foreach println
        case Left(e) ⇒ fail(e.reason)
      }
    }

    "Be marshalled with multiple should clauses" in {

      val query = Bool(
        Should(
          Prefix("name", "ev"),
          Prefix("city", "ev")
        )
      )

      val api = for {
        _ ← bulk(boolSpecsIndexActions: _*)
        r ← search[Json](idx, tpe, query, None)
        _ ← deleteIndex(idx)
      } yield r

      val response = client(api).futureValue
      println(s"RESPONSE: $response")
      response match {
        case Right(v) ⇒ v.documents foreach println
        case Left(e) ⇒ fail(e.reason)
      }

    }
  }

}
