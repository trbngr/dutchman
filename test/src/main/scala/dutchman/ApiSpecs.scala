package dutchman

import dutchman.api._
import dutchman.dsl._
import dutchman.model.Person
import dutchman.search._
import dutchman.model.Person
import dutchman.search.SearchSpecs
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global

trait ApiSpecs[Json]
  extends WordSpecLike
    with Matchers
    with ScalaFutures
    with BeforeAndAfterEach
    //    with IndexSpecs[Json]
    //    with BulkSpecs[Json]
        with SearchSpecs[Json]
    //    with ScrollSpecs[Json]
//    with BoolSpecs[Json]
{

  val tpe = Type("person")

  def readPerson(json: Json): Person

  val httpClient: HttpClient
  implicit val marshaller: marshalling.ApiMarshaller
  implicit val unMarshaller: marshalling.ApiUnMarshaller[Json]

  lazy implicit val dsl: Dsl[Json] = httpClient.bind(Endpoint.localhost, NullRequestSigner)

  def deleteIndex(idx: Idx) = DeleteIndex(idx).task.futureValue
  def refresh(index: Idx) = Refresh(index).task.futureValue

  implicit val patience = PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(50, Millis)))

  override protected def afterEach(): Unit = {
  }
}
