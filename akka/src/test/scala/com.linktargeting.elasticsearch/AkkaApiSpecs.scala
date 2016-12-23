package com.linktargeting.elasticsearch

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import cats.syntax.either._
import com.linktargeting.elasticsearch.http.circe._
import com.linktargeting.elasticsearch.http.marshalling._
import com.linktargeting.elasticsearch.model._
import io.circe.Json
import io.circe.generic.semiauto._
import org.scalatest.BeforeAndAfterAll

class AkkaApiSpecs extends TestKit(ActorSystem("AkkaApiSpecs")) with ApiSpecs[Json] with BeforeAndAfterAll {
  implicit val mat = ActorMaterializer()
  implicit val marshaller = CirceMarshaller
  implicit val unMarshaller = CirceUnmarshaller
  implicit val personDecoder = deriveDecoder[Person]

  val httpClient = new AkkaHttpClient()

  override protected def afterAll(): Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }
  override def readPerson(json: Json) = json.as[Person].getOrElse(throw DecodingError(s"can't read person: $json"))
}
