package dutchman.actor

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import dutchman.AkkaHttpClient
import dutchman.circe._
import dutchman.dsl._
import dutchman.ops._
import dutchman.http.Endpoint
import dutchman.model._
import io.circe.generic.semiauto.deriveDecoder
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.collection.immutable.Seq
import scala.concurrent.duration._
import scala.util.Random

class BulkIndexerSpecs extends TestKit(ActorSystem("BulkIndexerSpecs"))
  with ImplicitSender
  with FlatSpecLike
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll {

  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()
  implicit val personDecoder = deriveDecoder[Person]

  val httpClient = new AkkaHttpClient()
  val client = httpClient.bind(Endpoint.localhost)

  val idx = Idx("bulk_indexer_specs")
  val tpe = Type("person")

  def generatePeople(ids: Seq[Int]) = ids map { i ⇒
    Person(
      id = i.toString,
      name = Random.alphanumeric.take(5).mkString,
      city = Random.alphanumeric.take(3).mkString
    )
  }

  "BulkIndex" should "index documents" in {
    val indexer = TestActorRef(BulkIndexer.props(client, BulkIndexerConfig(flushDuration = 5 seconds, maxDocuments = 5)))

    val persons = generatePeople(1 to 18)

    persons foreach { p ⇒
      indexer ! BulkAction(Index(idx, tpe, p, None))
    }

    receiveN(18, 15 seconds) foreach println
  }

  override protected def afterAll(): Unit = {
    client(deleteIndex(idx)).futureValue
    TestKit.shutdownActorSystem(system)
  }
}
