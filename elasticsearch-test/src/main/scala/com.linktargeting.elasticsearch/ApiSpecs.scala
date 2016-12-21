package com.linktargeting.elasticsearch

import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.http._
import com.linktargeting.elasticsearch.model.Person
import com.linktargeting.elasticsearch.search._
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
    //    with SearchSpecs[Json]
    with ScrollSpecs[Json] {

  import http.syntax._

  val tpe = Type("person")

  def readPerson(json: Json): Person

  val httpClient: http.HttpClient
  implicit val marshaller: marshalling.ApiMarshaller
  implicit val unMarshaller: marshalling.ApiUnMarshaller[Json]

  lazy val client: ESClient[Json] = httpClient.connect(Endpoint.localhost, NullRequestSigner)

  def deleteIndex(idx: Idx) = client.indices(DeleteIndex(idx)).futureValue
  def refresh(index: Idx) = client.indices(Refresh(index)).futureValue

  implicit val patience = PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(50, Millis)))

  override protected def afterEach(): Unit = {
  }
}
