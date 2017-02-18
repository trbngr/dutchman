package dutchman

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.Materializer
import akka.util.ByteString
import dutchman.http._
import org.slf4j.LoggerFactory

import scala.concurrent.Future

object AkkaHttpClient {
  def apply()(implicit system: ActorSystem, mat: Materializer): AkkaHttpClient = new AkkaHttpClient()
}

class AkkaHttpClient(implicit val system: ActorSystem, mat: Materializer) extends HttpClient {

  val http = Http(system)
  implicit val ec = system.dispatcher
  val log = LoggerFactory.getLogger("dutchman.akka")

  def documentExists(endpoint: Endpoint)(request: Request): Future[Boolean] = {
    http.singleRequest(buildRequest(endpoint, request, includeEntity = false)) map { response ⇒
      log.debug(s"HTTPResponse: $response")
      if (response.status.isSuccess()) true else false
    }
  }

  def execute(endpoint: Endpoint)(request: Request): Future[String] = {
    http.singleRequest(buildRequest(endpoint, request)) flatMap {response ⇒
      log.debug(s"HTTPResponse: $response")
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

    val finalRequest = httpRequest.withHeaders(headers: _*)
    log.debug(s"HTTPRequest: $finalRequest")
    finalRequest
  }

  private implicit class RequestUriBuilder(request: Request) {
    def queryString: Option[String] = if (request.params.isEmpty) None else Some(Uri.Query(request.params).toString())
    def uri(endpoint: Endpoint): Uri = Uri.from(
      scheme = endpoint.protocol.toString,
      host = endpoint.host,
      port = endpoint.port,
      path = request.path,
      queryString = queryString
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
