package dutchman

import dutchman.api._
import dutchman.dsl._
import dutchman.model._
import dutchman.sprayjson._
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

class SprayJsonMarshallingSpecs extends FlatSpec with Matchers {
  val index = Idx("movies")
  val tpe = Type("movie")

  import SprayJsonOperationWriter.write
  import SprayJsonResponseReader.read

  "Get" should "be marshalled correctly" in {
    val api = Get(index, tpe, Id("123"))
    val json = write(api.data)
    json shouldBe empty
  }

  "Update" should "be marshalled correctly" in {
    val api = Update(index, tpe, Person("123", "Chris", "PHX"))
    val json = write(api.data)
    json shouldNot be(empty)
    read(json) shouldBe s"""{"id":"123", "name":"Chris", "city": "PHX"}""".parseJson
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

    val error = SprayJsonResponseReader.readError(read(js))
    error shouldBe Some(ESError("index_not_found_exception", "no such index", "index_or_alias", "1234", "people", 404))
  }

  "ESError" should "not be returned" in {
    val js =
      s"""
         |{
         |  "count": 4
         |}
       """.stripMargin

    SprayJsonResponseReader.readError(read(js)) shouldBe None
  }

  "SearchResponse" should "be unmarshalled" in {
    val js =
      s"""{
         |  "took": 3,
         |  "timed_out": false,
         |  "_shards": {
         |    "total": 1,
         |    "successful": 1,
         |    "failed": 0
         |  },
         |  "hits": {
         |    "total": 1,
         |    "max_score": 0.7245825,
         |    "hits": [
         |      {
         |        "_index": "movies",
         |        "_type": "movie",
         |        "_id": "1",
         |        "_score": 0.7245825,
         |        "_source": {
         |          "title": "Vision Loss after Intravitreal Injection of Autologous 'Stem Cells' for AMD."
         |        }
         |      }
         |    ]
         |  }
         |}
   """.stripMargin

    val response = SprayJsonResponseReader.search(read(js))
    val source = JsObject("title" -> JsString("Vision Loss after Intravitreal Injection of Autologous 'Stem Cells' for AMD."))
    response shouldBe SearchResponse[JsValue](Shards(1, 0 , 1), 1, Seq(JsonDocument[JsValue](index, tpe, Id("1"), 0.7245825.toFloat, source)))
  }


}
