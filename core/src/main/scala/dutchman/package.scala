
import cats.free.Free
import cats.instances.future._
import dutchman.dsl._
import dutchman.http._
import dutchman.marshalling._

import scala.concurrent.{ExecutionContext, Future}

package object dutchman {
  type ElasticOps[A] = Free[ElasticOp, A]

  implicit class RichClient[Json](client: HttpClient)(implicit ec: ExecutionContext, writer: ApiDataWriter, reader: ResponseReader[Json]) {
    def bind(endpoint: Endpoint, signer: ElasticRequestSigner = NullRequestSigner) = new ElasticClient[Json](client, endpoint, signer)
  }

  final class ElasticClient[Json](client: HttpClient, endpoint: Endpoint, signer: ElasticRequestSigner)
                                 (implicit ec: ExecutionContext, writer: ApiDataWriter, reader: ResponseReader[Json]) {

    private val interpreter = new Interpreter[Json](client, endpoint, signer)

    def apply[A](ops: ElasticOps[A]): Future[A] = execute(ops)
    def execute[A](ops: ElasticOps[A]): Future[A] = ops.foldMap(interpreter)
  }
}
