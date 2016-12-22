package com.linktargeting.elasticsearch

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.Materializer
import akka.util.ByteString
import com.linktargeting.elasticsearch.http._
import org.slf4j.LoggerFactory

import scala.concurrent.Future

object AkkaHttpClient {
  def apply()(implicit system: ActorSystem, mat: Materializer): AkkaHttpClient = new AkkaHttpClient()
}

class AkkaHttpClient(implicit val system: ActorSystem, mat: Materializer) extends http.HttpClient {

  import marshalling._

  val http = Http(system)
  implicit val ec = system.dispatcher
  val logger = LoggerFactory.getLogger(getClass)

  def execute[Json](endpoint: Endpoint, signer: ESRequestSigner)(request: Request)(implicit marshaller: ApiMarshaller, unMarshaller: ApiUnMarshaller[Json]): Future[Json] = {
    http.singleRequest(buildRequest(endpoint, signer, request)) flatMap { response =>
      response.entity.json { response ⇒
        val json = unMarshaller.read(response)
        logger.debug(s"ES Response: : $json")
        unMarshaller.readError(json) match {
          case Some(errors) ⇒ throw ESErrorsException(errors)
          case _            ⇒ json
        }
      }
    }
  }

  private def buildRequest(endpoint: Endpoint, signer: ESRequestSigner, request: Request): HttpRequest = {
    val signed = signer.sign(endpoint, request)
    val entity = HttpEntity(signed.payload)
    val uri = request.uri(endpoint)
    val unauthed = HttpRequest(method = request.verb, uri = uri, entity = entity)

    logger.debug(s"request: ${unauthed.method} $uri")
    logger.debug(s"payload: ${signed.payload}")

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
