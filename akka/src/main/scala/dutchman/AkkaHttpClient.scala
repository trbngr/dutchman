package dutchman

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.Materializer
import akka.util.ByteString
import dutchman.api._
import dutchman.http._

import scala.concurrent.Future

object AkkaHttpClient {
  def apply()(implicit system: ActorSystem, mat: Materializer): AkkaHttpClient = new AkkaHttpClient()
}

class AkkaHttpClient(implicit val system: ActorSystem, mat: Materializer) extends HttpClient {

  import marshalling._

  val http = Http(system)
  implicit val ec = system.dispatcher

  def documentExists(endpoint: Endpoint)(request: Request) = {
    http.singleRequest(buildRequest(endpoint, request, includeEntity = false)) map { response ⇒
      if (response.status.isSuccess()) true else false
    }
  }

  def execute[Json](endpoint: Endpoint)(request: Request)(implicit unMarshaller: ApiUnMarshaller[Json]): Future[Json] = {
    http.singleRequest(buildRequest(endpoint, request)) flatMap { response =>
      response.entity.json { response ⇒
        val json = unMarshaller.read(response)
        unMarshaller.readError(json) match {
          case Some(errors) ⇒ throw ESErrorsException(errors)
          case _            ⇒ json
        }
      }
    }
  }

  private def buildRequest(endpoint: Endpoint, request: Request, includeEntity: Boolean = true): HttpRequest = {
    val entity = HttpEntity(request.payload)
    val uri = request.uri(endpoint)
    val unauthed = {
      val req = HttpRequest(method = request.verb, uri = uri)
      if(includeEntity)
        req.copy(entity = entity)
      else
        req
    }

    val hostHeader = request.headers.find(_.name == "Host").map(x ⇒ Host(x.value))
    val headers = request.headers.filterNot(_.name == "Host").map(x ⇒ RawHeader(x.name, x.value)) ++ hostHeader

    unauthed.withHeaders(headers: _*)
  }

  private implicit class RequestUriBuilder(request: Request) {
    def uri(endpoint: Endpoint) = Uri.from(
      scheme = endpoint.port match {
        case 443 => "https"
        case _   => "http"
      },
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

  private implicit class entityToJson(entity: HttpEntity) {
    def json[T](f: String ⇒ T): Future[T] = {
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String) map f
    }
  }
}
