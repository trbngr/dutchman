package dutchman

import dutchman.document._
import dutchman.dsl._
import dutchman.http.{Endpoint, HttpClient}
import dutchman.model._
import dutchman.ops._
import dutchman.search._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global

trait ApiSpecs[Json]
  extends WordSpecLike
    with Matchers
    with ScalaFutures
    with BeforeAndAfterEach
    with IndexSpecs[Json]
    with BulkSpecs[Json]
    with SearchSpecs[Json]
    with ScrollSpecs[Json]
    with GetSpecs[Json]
    with BoolSpecs[Json] {

  def readPerson(json: Json): Person

  val httpClient: HttpClient
  implicit val dataWriter: marshalling.ApiDataWriter
  implicit val responseReader: marshalling.ResponseReader[Json]

  lazy implicit val client = httpClient.bind(Endpoint.localhost)

  implicit val patience = PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(50, Millis)))

  "DocumentExists" when {
    val idx: Idx = "document_exists_test"
    val tpe: Type = "document"
    val id: Id = "123"

    "a document doesn't exist" should {
      "return false" in {
        client(documentExists(idx, tpe, id)) map { exists ⇒
          exists shouldBe false
        }
      }
    }

    "a document does exist" should {
      "return true" in {

        val api = for {
          _ ← index(idx, tpe, ElasticDocument(id, Map("name" → "chris")), None)
          _ ← refresh(idx)
          e ← documentExists(idx, tpe, id)
          _ ← deleteIndex(idx)
        } yield e

        client(api) map { exists ⇒
          exists shouldBe true
        }
      }
    }
  }

  override protected def afterEach(): Unit = {
  }
}
