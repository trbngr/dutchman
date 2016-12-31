package dutchman

import scala.concurrent.duration._

package object api extends search with query with syntax {

  sealed trait Api[+A]
  sealed trait DocumentApi
  sealed trait SingleDocumentApi extends DocumentApi
  sealed trait IndicesApi
  sealed trait SearchApi

  case class Shards(total: Int, failed: Int, successful: Int)
  case class Response(shards: Shards, index: String, `type`: String, id: String, version: Int)
  case class ESError(index: String, `type`: String, id: String, status: Int)
  case class ESErrorsException(errors: Seq[ESError]) extends Exception(s"Elasticsearch exception: ${errors.map(e ⇒ e.status).mkString("\n")}")

  final case class Id(value: String)

  object Idx {
    def apply(name: String): Idx = new Idx(name.toLowerCase())
  }
  final class Idx(val name: String)

  final case class Type(name: String)

  trait ESDocument[A] {
    def document(a: A): Document
  }

  implicit val defaultDocument = new ESDocument[Document] {
    def document(a: Document) = a
  }

  //document api
  case class Document(id: Id, data: Map[String, Any])

  object Index {
    def apply[A: ESDocument](index: Idx, `type`: Type, document: A, version: Option[Int] = None): Index = new Index(index, `type`, implicitly[ESDocument[A]].document(document), version)
  }

  object Update {
    def apply[A: ESDocument](index: Idx, `type`: Type, document: A): Update = new Update(index, `type`, implicitly[ESDocument[A]].document(document))
  }

  case class DocumentExists(index: Idx, `type`: Type, id: Id) extends Api[Boolean] with DocumentApi
  case class Index(index: Idx, `type`: Type, document: Document, version: Option[Int]) extends Api[IndexResponse] with SingleDocumentApi
  case class Get[Json](index: Idx, `type`: Type, id: Id) extends Api[GetResponse[Json]] with SingleDocumentApi
  case class Delete(index: Idx, `type`: Type, id: Id, version: Option[Int]) extends Api[DeleteResponse] with SingleDocumentApi
  case class Update(index: Idx, `type`: Type, document: Document) extends Api[UpdateResponse] with SingleDocumentApi
  case class MultiGet(ids: (Idx, Option[Type], Option[Id])*) extends Api[MultiGetResponse] with DocumentApi

  sealed trait BulkAction
  case object BulkIndex extends BulkAction
  case object BulkCreate extends BulkAction
  case object BulkDelete extends BulkAction
  case object BulkUpdate extends BulkAction

  object BulkAction {
    def apply(c: Index, create: Boolean = false): (BulkAction, SingleDocumentApi) = (if (create) BulkCreate else BulkIndex) → c
    def apply(c: Update): (BulkAction, SingleDocumentApi) = BulkUpdate → c
    def apply(c: Delete): (BulkAction, SingleDocumentApi) = BulkDelete → c
  }

  case class Bulk(actions: (BulkAction, SingleDocumentApi)*) extends Api[Seq[BulkResponse]] with DocumentApi
  case class BulkResponse(action: BulkAction, status: Int, response: Response)

  case class IndexResponse(created: Boolean, response: Response)
  case class DeleteResponse()
  case class UpdateResponse()
  case class MultiGetResponse()
  case class GetResponse[Json](index: String, `type`: String, id: String, version: Int, found: Boolean, source: Json)

  //indices api
  case class DeleteIndex(index: Idx) extends Api[DeleteIndexResponse] with IndicesApi
  case class DeleteIndexResponse(acknowledged: Boolean)
  case class Refresh(indices: Seq[Idx]) extends Api[RefreshResponse] with IndicesApi
  case class RefreshResponse(shards: Shards)

  //search api
  case class Search[Json](indices: Seq[Idx], types: Seq[Type], query: Query, options: Option[SearchOptions]) extends Api[SearchResponse[Json]] with SearchApi

  case class JsonDocument[Json](index: Idx, `type`: Type, id: Id, score: Float, source: Json)
  case class SearchResponse[Json](shards: Shards, total: Int, documents: Seq[JsonDocument[Json]])

  case class StartScroll[Json](index: Idx, `type`: Type, query: Query, ttl: FiniteDuration = 1 minute, options: Option[SearchOptions]) extends Api[ScrollResponse[Json]] with SearchApi
  case class Scroll[Json](scrollId: String, ttl: FiniteDuration = 1 minute) extends Api[ScrollResponse[Json]] with SearchApi

  object ClearScroll {
    def apply(scrollId: String): ClearScroll = new ClearScroll(Set(scrollId))
  }
  case class ClearScroll(scrollIds: Set[String]) extends Api[Unit] with SearchApi

  case class ScrollResponse[Json](scrollId: String, results: SearchResponse[Json])

}
