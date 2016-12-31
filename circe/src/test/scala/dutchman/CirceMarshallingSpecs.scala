package dutchman

import dutchman.api._
import dutchman.model._
import cats.syntax.either._
import dutchman.circe.CirceOperationWriter
import io.circe.Json
import io.circe.parser._
import org.scalatest.{FlatSpec, Matchers}

class CirceMarshallingSpecs extends FlatSpec with Matchers {
  val index = Idx("movies")
  val tpe = Type("movie")

  "Get" should "be marshalled correctly" in {
    val api = Get(index, tpe, Id("123"))
    val json = CirceOperationWriter.write(api)
    json shouldBe empty
  }

  "Update" should "be marshalled correctly" in {
    val api = Update(index, tpe, Person("123", "Chris", "PHX"))
    val json = CirceOperationWriter.write(api)
    json shouldNot be(empty)
    json shouldBe parse(s"""{"name":"Chris", "city": "PHX"}""").getOrElse(Json.Null)
  }
}
