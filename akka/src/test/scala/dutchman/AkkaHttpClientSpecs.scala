package dutchman

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import org.scalatest.BeforeAndAfterAll

class AkkaHttpClientSpecs extends TestKit(ActorSystem("AkkaHttpClientSpecs")) with HttpClientSpecs with BeforeAndAfterAll {
  implicit val mat = ActorMaterializer()
  val httpClient = new AkkaHttpClient()

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)
}