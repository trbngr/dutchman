import cats.free.Free
import cats.instances.future._
import cats.~>
import dutchman.api._
import dutchman.http._
import dutchman.marshalling._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

package object dutchman {

  type ESApi[A] = Free[Api, A]

  implicit class RichHttpClient[Json](client: HttpClient)(implicit ec: ExecutionContext, marshaller: ApiMarshaller, unMarshaller: ApiUnMarshaller[Json]) {
    def bind(endpoint: Endpoint, signer: ESRequestSigner = NullRequestSigner) = new ESClient(client, endpoint, signer)
  }

  final class ESClient[Json](client: HttpClient, endpoint: Endpoint, signer: ESRequestSigner)
                            (implicit ec: ExecutionContext, marshaller: ApiMarshaller, unMarshaller: ApiUnMarshaller[Json]) extends OperationDefinitions[Json, Future] {

    private def request[A](api: Api[A], fx: Json ⇒ A): Future[A] = {
      val request = signer.sign(endpoint, formatting.apiRequest(api))
      client.execute(endpoint)(request) map fx
    }

    private val compiler = new (Api ~> Future) {
      def apply[A](fa: Api[A]) = {
        val future = fa match {
          case x: Bulk           ⇒ request(x, unMarshaller.bulk)
          case x: ClearScroll    ⇒ request(x, _ ⇒ ())
          case x: Delete         ⇒ request(x, unMarshaller.delete)
          case x: DeleteIndex    ⇒ request(x, unMarshaller.deleteIndex)
          case x: DocumentExists ⇒ client.documentExists(endpoint)(signer.sign(endpoint, formatting.apiRequest(x)))
          case x: Get[_]         ⇒ request(x, unMarshaller.get)
          case x: Index          ⇒ request(x, unMarshaller.index)
          case x: MultiGet       ⇒ request(x, unMarshaller.multiGet)
          case x: Refresh        ⇒ request(x, unMarshaller.refresh)
          case x: Scroll[_]      ⇒ request(x, unMarshaller.scroll)
          case x: Search[_]      ⇒ request(x, unMarshaller.search)
          case x: StartScroll[_] ⇒ request(x, unMarshaller.scroll)
          case x: Update         ⇒ request(x, unMarshaller.update)
        }
        future map (_.asInstanceOf[A])
      }

    }

    def apply[A](api: ESApi[A]): Future[A] = execute(api)
    def execute[A](api: ESApi[A]): Future[A] = api.foldMap(compiler)

    val ops = new Operations[Json]

    def bulk(actions: (BulkAction, SingleDocumentApi)*) = execute(ops.bulk(actions: _*))
    def clearScroll(scrollIds: Set[String]) = execute(ops.clearScroll(scrollIds))
    def delete(index: Idx, `type`: Type, id: Id, version: Option[Int]) = execute(ops.delete(index, `type`, id, version))
    def deleteIndex(index: Idx) = execute(ops.deleteIndex(index))
    def documentExists(index: Idx, `type`: Type, id: Id) = execute(ops.documentExists(index, `type`, id))
    def get(index: Idx, `type`: Type, id: Id) = execute(ops.get(index, `type`, id))
    def index[A: ESDocument](index: Idx, `type`: Type, document: A, version: Option[Int]) = execute(ops.index(index, `type`, document, version))
    def multiGet(ids: (Idx, Option[Type], Option[Id])*) = execute(ops.multiGet(ids: _*))
    def refresh(indices: Seq[Idx]) = execute(ops.refresh(indices))
    def refresh(index: Idx) = execute(ops.refresh(index))
    def scroll(scrollId: String, ttl: FiniteDuration) = execute(ops.scroll(scrollId, ttl))
    def search(index: Idx, `type`: Type, query: Query, options: Option[SearchOptions]) = execute(ops.search(index, `type`, query, options))
    def search(index: Idx, types: Seq[Type], query: Query, options: Option[SearchOptions]) = execute(ops.search(index, types, query, options))
    def search(indices: Seq[Idx], `type`: Type, query: Query, options: Option[SearchOptions]) = execute(ops.search(indices, `type`, query, options))
    def search(indices: Seq[Idx], types: Seq[Type], query: Query, options: Option[SearchOptions]) = execute(ops.search(indices, types, query, options))
    def startScroll(index: Idx, `type`: Type, query: Query, ttl: FiniteDuration, options: Option[SearchOptions]) = execute(ops.startScroll(index, `type`, query, ttl, options))
    def update[A: ESDocument](index: Idx, `type`: Type, document: A) = execute(ops.update(index, `type`, document))
  }

}
