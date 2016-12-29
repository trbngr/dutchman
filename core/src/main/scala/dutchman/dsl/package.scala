package dutchman

import dutchman.api._
import dutchman.marshalling.{ApiMarshaller, ApiUnMarshaller}

import scala.concurrent.{ExecutionContext, Future}

package object dsl extends dsl.syntax {
  trait Dsl[Json] {
    val document: DocumentApiClient[Json]
    val indices: IndicesApiClient[Json]
    val search: SearchApi[Json]
  }

  trait SearchApi[Json] {
    def apply(api: Search): Future[SearchResponse[Json]]
    def apply(api: StartScroll): Future[ScrollResponse[Json]]
    def apply(api: Search, options: SearchOptions): Future[SearchResponse[Json]]
    def apply(api: StartScroll, options: SearchOptions): Future[ScrollResponse[Json]]
    def apply(api: Scroll): Future[ScrollResponse[Json]]
    def apply(api: ClearScroll): Future[Unit]
  }

  trait DocumentApiClient[Json] {
    def apply(api: Index): Future[IndexResponse]
    def apply(api: Bulk): Future[Seq[BulkResponse]]
  }

  trait IndicesApiClient[Json] {
    def apply(api: DeleteIndex): Future[DeleteIndexResponse]
    def apply(api: Refresh): Future[RefreshResponse]
  }

  implicit class ClientDsl(client: HttpClient) {
    def bind[Json](endpoint: Endpoint, signer: ESRequestSigner = NullRequestSigner)(implicit ec: ExecutionContext, marshaller: ApiMarshaller, unMarshaller: ApiUnMarshaller[Json]): Dsl[Json] = {
      new BoundDsl[Json](client, endpoint, signer)
    }
  }

  final class BoundDsl[Json](client: HttpClient, endpoint: Endpoint, signer: ESRequestSigner = NullRequestSigner)(implicit ec: ExecutionContext, marshaller: ApiMarshaller, unMarshaller: ApiUnMarshaller[Json])
    extends Dsl[Json]
      with DocumentApiClient[Json]
      with IndicesApiClient[Json]
      with SearchApi[Json] {

    private def exe[A <: Api, T](api: A, f: Json ⇒ T) = {
      client.execute[Json](endpoint, signer)(api.request) map (f(_))
    }

    val document = this
    val indices = this
    val search  = this

    def apply(api: Index): Future[IndexResponse] = exe(api, unMarshaller.index)
    def apply(api: Bulk): Future[Seq[BulkResponse]] = exe(api, unMarshaller.bulk)
    def apply(api: DeleteIndex): Future[DeleteIndexResponse] = exe(api, unMarshaller.deleteIndex)
    def apply(api: Refresh): Future[RefreshResponse] = exe(api, unMarshaller.refresh)
    def apply(api: Scroll): Future[ScrollResponse[Json]] = exe(api, unMarshaller.scroll)
    def apply(api: ClearScroll): Future[Unit] = exe(api, _ ⇒ ())

    def apply(api: Search): Future[SearchResponse[Json]] = exe(api, unMarshaller.search)
    def apply(api: StartScroll): Future[ScrollResponse[Json]] = exe(api, unMarshaller.scroll)
    def apply(api: Search, options: SearchOptions): Future[SearchResponse[Json]] = exe(api.copy(query = QueryWithOptions(api.query, options)), unMarshaller.search)
    def apply(api: StartScroll, options: SearchOptions): Future[ScrollResponse[Json]] = exe(api.copy(query = QueryWithOptions(api.query, options)), unMarshaller.scroll)
  }
}
