package dutchman

import scala.concurrent.duration._

package object api extends search with query with syntax {

  sealed trait Api[A]
  sealed trait DocumentApi[A] extends Api[A]
  sealed trait SingleDocumentApi[A] extends DocumentApi[A]
  sealed trait BulkAction
  sealed trait IndicesApi[A] extends Api[A]
  sealed trait SearchApi[A] extends Api[A]

  sealed trait SearchApiWithOptions {
    val query: Query
  }

  type DataContainer = Map[String, Any]

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

  //document api
  case class Document(id: Id, data: DataContainer)

  object Index {
    def apply[A: ESDocument](index: Idx, `type`: Type, document: A, version: Option[Int] = None): Index = new Index(index, `type`, implicitly[ESDocument[A]].document(document), version)
  }

  object Update {
    def apply[A: ESDocument](index: Idx, `type`: Type, document: A): Update = new Update(index, `type`, implicitly[ESDocument[A]].document(document))
  }

  case class DocumentExists(index: Idx, `type`: Type, id: Id) extends DocumentApi[Boolean]
  case class Index(index: Idx, `type`: Type, document: Document, version: Option[Int]) extends SingleDocumentApi[IndexResponse]
  case class Get[Json](index: Idx, `type`: Type, id: Id) extends SingleDocumentApi[GetResponse[Json]]
  case class Delete(index: Idx, `type`: Type, id: Id, version: Option[Int]) extends SingleDocumentApi[DeleteResponse]
  case class Update(index: Idx, `type`: Type, document: Document) extends SingleDocumentApi[UpdateResponse]
  case class MultiGet(ids: (Idx, Option[Type], Option[Id])*) extends DocumentApi[MultiGetResponse]

  case object BulkIndex extends BulkAction
  case object BulkCreate extends BulkAction
  case object BulkDelete extends BulkAction
  case object BulkUpdate extends BulkAction

  object Bulk {
    def apply(c: Index, create: Boolean = false): (BulkAction, Index) = (if (create) BulkCreate else BulkIndex) → c
    def apply(c: Update): (BulkAction, Update) = BulkUpdate → c
    def apply(c: Delete): (BulkAction, Delete) = BulkDelete → c
  }

  case class Bulk[A, B: SingleDocumentApi[_]](actions: (BulkAction, B)*) extends DocumentApi[A]

  case class BulkResponse(action: BulkAction, status: Int, response: Response) 

  case class IndexResponse(created: Boolean, response: Response)
  case class DeleteResponse()
  case class UpdateResponse()
  case class MultiGetResponse()
  case class GetResponse[Json](index: String, `type`: String, id: String, version: Int, found: Boolean, source: Json)

  //indices api
  case class DeleteIndex(index: Idx) extends IndicesApi[DeleteIndexResponse]
  case class DeleteIndexResponse(acknowledged: Boolean) 

  object Refresh {
    def apply(index: Idx): Refresh = Refresh(Seq(index))

    def apply(): Refresh = Refresh(Seq.empty)
  }
  case class Refresh(indices: Seq[Idx]) extends IndicesApi[RefreshResponse]
  case class RefreshResponse(shards: Shards) 

  //search api

  object Search {
    def apply[Json](index: Idx, `type`: Type, query: Query): Search[Json] = new Search(Seq(index), Seq(`type`), query)
    def apply[Json](indices: Seq[Idx], `type`: Type, query: Query): Search[Json] = new Search(indices, Seq(`type`), query)
    def apply[Json](index: Idx, types: Seq[Type], query: Query): Search[Json] = new Search(Seq(index), types, query)
  }
  case class Search[Json](indices: Seq[Idx], types: Seq[Type], query: Query) extends SearchApi[SearchResponse[Json]] with SearchApiWithOptions

  case class JsonDocument[Json](index: Idx, `type`: Type, id: Id, score: Float, source: Json)
  case class SearchResponse[Json](shards: Shards, total: Int, documents: Seq[JsonDocument[Json]]) 

  case class StartScroll[Json](index: Idx, `type`: Type, query: Query, ttl: FiniteDuration = 1 minute) extends SearchApi[ScrollResponse[Json]] with SearchApiWithOptions
  case class Scroll[Json](scrollId: String, ttl: FiniteDuration = 1 minute) extends SearchApi[ScrollResponse[Json]]

  object ClearScroll {
    def apply(scrollId: String): ClearScroll = new ClearScroll(Set(scrollId))
  }
  case class ClearScroll(scrollIds: Set[String]) extends SearchApi[Unit]

  case class ScrollResponse[Json](scrollId: String, results: SearchResponse[Json]) 

}
