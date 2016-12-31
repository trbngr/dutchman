package dutchman

import dutchman.marshalling.ResponseReader

import scala.concurrent.Future

object http {

  sealed trait Verb
  case object GET extends Verb
  case object HEAD extends Verb
  case object POST extends Verb
  case object PUT extends Verb
  case object DELETE extends Verb

  case class Header(name: String, value: String)
  case class Request(verb: Verb, path: String, params: Map[String, String] = Map.empty, headers: Seq[Header] = Seq.empty, payload: String = "")

  object Endpoint {
    val localhost: Endpoint = localhost(9200)
    def localhost(port: Int): Endpoint = Endpoint("localhost", port)
  }
  case class Endpoint(host: String, port: Int)

  trait ESRequestSigner {
    def sign(endpoint: Endpoint, request: Request): Request
  }

  object NullRequestSigner extends ESRequestSigner {
    def sign(endpoint: Endpoint, request: Request) = request
  }

  trait HttpClient {
    def execute(endpoint: Endpoint)(request: Request): Future[String]
    def documentExists(endpoint: Endpoint)(request: Request): Future[Boolean]
  }

  case class HttpError(message: String) extends Exception(message)
}