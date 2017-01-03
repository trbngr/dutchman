package dutchman

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import cats.syntax.either._
import dutchman.circe._
import dutchman.marshalling._
import dutchman.model.Person
import io.circe.Json
import io.circe.generic.semiauto._
import org.scalatest.BeforeAndAfterAll

class AkkaApiSpecs extends TestKit(ActorSystem("AkkaApiSpecs")) with ApiSpecs[Json] with BeforeAndAfterAll {
  implicit val mat = ActorMaterializer()
  implicit val dataWriter = CirceOperationWriter
  implicit val responseReader = CirceResponseReader
  implicit val personDecoder = deriveDecoder[Person]

  val httpClient = new AkkaHttpClient()

  override protected def afterAll(): Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }
  override def readPerson(json: Json) = json.as[Person].getOrElse(throw DecodingError(s"can't read person: $json"))
}
