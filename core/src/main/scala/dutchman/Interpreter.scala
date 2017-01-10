package dutchman

import cats.~>
import dutchman.api._
import dutchman.dsl._
import dutchman.http._
import dutchman.marshalling.{ApiDataWriter, ResponseReader}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class Interpreter[Json](client: HttpClient, endpoint: Endpoint, signer: ElasticRequestSigner)
                       (implicit ec: ExecutionContext, writer: ApiDataWriter, reader: ResponseReader[Json])
  extends (ElasticOp ~> Future) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private def execute[A](api: ApiRepresentation, fx: Json ⇒ A): Future[ElasticResponse[A]] = {

    val payload = api.data.get(BulkActionsKey) collect {
      case c: Seq[_] ⇒ writer.write(c.asInstanceOf[Seq[ApiData]])
    } getOrElse writer.write(api.data)

    println(s"request: ${api.request.verb} ${api.request.path}: $payload`")
    logger.debug(s"request: ${api.request.verb} ${api.request.path}: $payload`")

    val request = signer.sign(endpoint, api.request).copy(
      payload = payload
    )

    client.execute(endpoint)(request) map { response ⇒
      val json = reader.read(response)
      println(s"response: $json")
      logger.debug(s"response: $json")
      reader.readError(json) match {
        case Some(error) ⇒ Left(error)
        case _           ⇒ Try(fx(json)) match {
          case Success(value) ⇒ Right(value)
          case Failure(e)     ⇒ Left(ESError("marshalling", e.getMessage, "", "", "", -6))
        }
      }
    } recover {
      case e: Throwable ⇒ Left(ESError("http", e.getMessage, "", "", "", -5))
    }
  }

  def apply[A](op: ElasticOp[A]): Future[A] = {
    val api = op.api
    (op match {
      case _: DocumentExists ⇒ client.documentExists(endpoint)(api.request)
        .recover({ case e: Throwable ⇒
            logger.warn(s"document exists error: ${e.getMessage}")
            false
        })
      case _: Bulk           ⇒ execute(api, reader.bulk)
      case _: Delete         ⇒ execute(api, reader.delete)
      case _: Get[_]         ⇒ execute(api, reader.get)
      case _: Index          ⇒ execute(api, reader.index)
      case _: MultiGet[_]    ⇒ execute(api, reader.multiGet)
      case _: Update         ⇒ execute(api, reader.update)
      case _: DeleteIndex    ⇒ execute(api, reader.deleteIndex)
      case _: Refresh        ⇒ execute(api, reader.refresh)
      case _: Search[_]      ⇒ execute(api, reader.search)
    }) map (_.asInstanceOf[A])
  }
}
