package dutchman

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FlatSpecLike, Matchers}

trait HttpClientSpecs extends FlatSpecLike with Matchers with ScalaFutures with JsonLoader {

  import testMarshalling._

  val httpClient: HttpClient

  implicit val patience = PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(50, Millis)))

  "HttpClient" should "return Json" in {
    val endpoint = Endpoint("raw.githubusercontent.com", 443)
    val path = "/trbngr/dutchman/master/test/src/main/resources/search_result.json"
    val future = httpClient.execute(endpoint)(Request(GET, path))
    val json = future.futureValue

    json shouldBe loadJson("search_result")
  }
}

