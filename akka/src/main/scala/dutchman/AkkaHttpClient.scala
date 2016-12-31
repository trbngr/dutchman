package dutchman

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.Materializer
import akka.util.ByteString
import dutchman.http._

import scala.concurrent.Future

object AkkaHttpClient {
  def apply()(implicit system: ActorSystem, mat: Materializer): AkkaHttpClient = new AkkaHttpClient()
}

class AkkaHttpClient(implicit val system: ActorSystem, mat: Materializer) extends HttpClient {

  val http = Http(system)
  implicit val ec = system.dispatcher

  def documentExists(endpoint: Endpoint)(request: Request) = {
    http.singleRequest(buildRequest(endpoint, request, includeEntity = false)) map { response ⇒
      if (response.status.isSuccess()) true else false
    }
  }

  def execute(endpoint: Endpoint)(request: Request): Future[String] = {
    http.singleRequest(buildRequest(endpoint, request)) flatMap {response ⇒
      response.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String)
    }
  }

  private def buildRequest(endpoint: Endpoint, request: Request, includeEntity: Boolean = true): HttpRequest = {
    val entity = HttpEntity(request.payload)
    val uri = request.uri(endpoint)

    val httpRequest = {
      val req = HttpRequest(method = request.verb, uri = uri)
      if (includeEntity)
        req.copy(entity = entity)
      else
        req
    }

    val hostHeader = request.headers.find(_.name == "Host").map(x ⇒ Host(x.value))
    val headers = request.headers.filterNot(_.name == "Host").map(x ⇒ RawHeader(x.name, x.value)) ++ hostHeader

    httpRequest.withHeaders(headers: _*)
  }

  private implicit class RequestUriBuilder(request: Request) {
    def uri(endpoint: Endpoint) = Uri.from(
      scheme = endpoint.protocol,
      host = endpoint.host,
      port = endpoint.port,
      path = request.path,
      queryString = Some(Uri.Query(request.params).toString())
    )
  }

  private implicit def verbToMethod(verb: Verb): HttpMethod = verb match {
    case GET    ⇒ HttpMethods.GET
    case POST   ⇒ HttpMethods.POST
    case PUT    ⇒ HttpMethods.PUT
    case DELETE ⇒ HttpMethods.DELETE
    case HEAD   ⇒ HttpMethods.HEAD
  }
}
