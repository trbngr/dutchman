package com.linktargeting.elasticsearch

import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.api.translation._
import com.linktargeting.elasticsearch.http._
import com.linktargeting.elasticsearch.http.marshalling._

import scala.concurrent.{ExecutionContext, Future}

object client {
  trait ESClient[Json] {

    val document: DocumentApiClient[Json]
    val indices: IndicesApiClient[Json]

    def apply(api: Search): Future[SearchResponse[Json]]
    def apply(api: StartScroll): Future[ScrollResponse[Json]]
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

  implicit class ClientSugar(client: HttpClient) {
    def connect[Json](endpoint: Endpoint, signer: ESRequestSigner = NullRequestSigner)(implicit ec: ExecutionContext, marshaller: ApiMarshaller, unMarshaller: ApiUnMarshaller[Json]): ESClient[Json] = {
      new ConnectedClient[Json](client, endpoint, signer)
    }
  }

  final class ConnectedClient[Json](client: HttpClient, endpoint: Endpoint, signer: ESRequestSigner = NullRequestSigner)(implicit ec: ExecutionContext, marshaller: ApiMarshaller, unMarshaller: ApiUnMarshaller[Json])
    extends ESClient[Json]
      with DocumentApiClient[Json]
      with IndicesApiClient[Json] {

    import com.linktargeting.elasticsearch.http.marshalling.syntax._

    private def exe(api: Api) = client.execute[Json](endpoint, signer)(apiRequest(api))

    val document = this
    val indices = this

    def apply(api: Index): Future[IndexResponse] = exe(api).map(api.response(_))
    def apply(api: Bulk): Future[Seq[BulkResponse]] = exe(api).map(api.response(_))
    def apply(api: DeleteIndex): Future[DeleteIndexResponse] = exe(api).map(api.response(_))
    def apply(api: Refresh): Future[RefreshResponse] = exe(api).map(api.response(_))
    def apply(api: Search): Future[SearchResponse[Json]] = exe(api).map(api.response(_))
    def apply(api: StartScroll): Future[ScrollResponse[Json]] = exe(api).map(api.response(_))
    def apply(api: Scroll): Future[ScrollResponse[Json]] = exe(api).map(api.response(_))
    def apply(api: ClearScroll): Future[Unit] = exe(api) map (_ ⇒ ())
  }
}
