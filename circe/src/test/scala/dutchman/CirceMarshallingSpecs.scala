package dutchman

import cats.syntax.either._
import dutchman.circe._
import dutchman.api._
import dutchman.dsl._
import dutchman.model._
import io.circe.Json
import io.circe.parser._
import org.scalatest.{FlatSpec, Matchers}

class CirceMarshallingSpecs extends FlatSpec with Matchers {
  val index = Idx("movies")
  val tpe = Type("movie")

  import CirceOperationWriter.write
  import CirceResponseReader.read

  "Get" should "be marshalled correctly" in {
    val api = Get(index, tpe, Id("123"))
    val json = write(api.data)
    json shouldBe empty
  }

  "Update" should "be marshalled correctly" in {
    val api = Update(index, tpe, Person("123", "Chris", "PHX"))
    val json = write(api.data)
    json shouldNot be(empty)
    read(json) shouldBe parse(s"""{"id":"123", "name":"Chris", "city": "PHX"}""").getOrElse(Json.Null)
  }
}
