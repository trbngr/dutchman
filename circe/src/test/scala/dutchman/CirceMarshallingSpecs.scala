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

  "ESError" should "be unmarshalled" in {
    val js =
      s"""
         |{
         |  "error" : {
         |    "type" : "index_not_found_exception",
         |    "reason" : "no such index",
         |    "resource.type" : "index_or_alias",
         |    "resource.id" : "1234",
         |    "index" : "people"
         |  },
         |  "status" : 404
         |}
       """.stripMargin

    CirceResponseReader.readError(read(js)) shouldBe Some(ESError("index_not_found_exception", "no such index", "index_or_alias", "1234", "people", 404))
  }

  "ESError" should "not be returned" in {
    val js =
      s"""
         |{
         |  "count": 4
         |}
       """.stripMargin

    CirceResponseReader.readError(read(js)) shouldBe None

  }
}
