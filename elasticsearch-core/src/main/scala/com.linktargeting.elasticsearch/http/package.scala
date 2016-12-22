package com.linktargeting.elasticsearch

import com.linktargeting.elasticsearch.api.ESError
import com.linktargeting.elasticsearch.http.marshalling.{ApiMarshaller, ApiUnMarshaller}

import scala.concurrent.Future

package object http {
  sealed trait Verb
  case object GET extends Verb
  case object HEAD extends Verb
  case object POST extends Verb
  case object PUT extends Verb
  case object DELETE extends Verb

  case class Header(name: String, value: String)
  case class Request(verb: Verb, path: String, params: Map[String, String] = Map.empty, headers: Seq[Header] = Seq.empty, payload: String = "")

  object Endpoint{
    val localhost: Endpoint = localhost(9200)
    def localhost(port: Int): Endpoint = Endpoint("localhost", port)
  }
  case class Endpoint(host: String, port: Int)

  trait ESRequestSigner {
    def sign(endpoint: Endpoint, request: Request): Request
  }

  case object NullRequestSigner extends ESRequestSigner {
    override def sign(endpoint: Endpoint, request: Request) = request
  }

  case class ESErrorsException(errors: Seq[ESError]) extends Exception(s"Elasticsearch exception: ${errors.map(e ⇒ e.status).mkString("\n")}")

  trait HttpClient {
    def execute[Json](endpoint: Endpoint, signer: ESRequestSigner)(request: Request)
                     (implicit marshaller: ApiMarshaller, unMarshaller: ApiUnMarshaller[Json]): Future[Json]
  }
}
