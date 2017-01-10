package dutchman

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
    def localhost(port: Int): Endpoint = Endpoint("localhost", port, Http)
    def aws(host: String): Endpoint = Endpoint(host, 443, Https)
  }

  sealed trait Protocol
  case object Http extends Protocol{
    override def toString = "http"
  }
  case object Https extends Protocol{
    override def toString = "https"
  }

  case class Endpoint(host: String, port: Int, protocol: Protocol = Https)

  trait ElasticRequestSigner {
    def sign(endpoint: Endpoint, request: Request): Request
  }

  object NullRequestSigner extends ElasticRequestSigner {
    def sign(endpoint: Endpoint, request: Request): Request = request
  }

  trait HttpClient {
    def execute(endpoint: Endpoint)(request: Request): Future[String]
    def documentExists(endpoint: Endpoint)(request: Request): Future[Boolean]
  }
}
