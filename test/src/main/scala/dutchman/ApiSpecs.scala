package dutchman

import dutchman.api._
import dutchman.document._
import dutchman.http.{Endpoint, HttpClient}
import dutchman.model.Person
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
    with BoolSpecs[Json] {

  val tpe = Type("person")

  def readPerson(json: Json): Person

  val httpClient: HttpClient
  implicit val marshaller: marshalling.ApiMarshaller
  implicit val unMarshaller: marshalling.ApiUnMarshaller[Json]

  lazy implicit val client = httpClient.bind(Endpoint.localhost)

  def deleteIndex(idx: Idx) = client.deleteIndex(idx).futureValue
  def refresh(index: Idx) = client.refresh(Seq(index)).futureValue

  implicit val patience = PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(50, Millis)))

  "DocumentExists" when {
    val idx: Idx = "document_exists_test"
    val tpe: Type = "document"
    val id: Id = "123"

    "a document doesn't exist" should {
      "return false" in {
        client.documentExists(idx, tpe, id) map { exists ⇒
          exists shouldBe false
        }
      }
    }

    "a document does exist" should {
      "return true" in {

        val ops = client.ops

        val api = for {
          _ ← ops.index(idx, tpe, Document(id, Map("name" → "chris")), None)
          _ ← ops.refresh(idx)
          e ← ops.documentExists(idx, tpe, id)
          _ ← ops.deleteIndex(idx)
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
