package com.linktargeting.elasticsearch

import scala.concurrent.duration._

package object api {
  import translation._

  sealed trait Api
  sealed trait DocumentApi extends Api
  sealed trait SingleDocumentApi extends DocumentApi
  sealed trait BulkAction
  sealed trait IndicesApi extends Api
  sealed trait SearchApi extends Api

  sealed trait Query

  case class Shards(total: Int, failed: Int, successful: Int)
  case class Response(shards: Shards, index: String, `type`: String, id: String, version: Int)
  case class EsError(index: String, `type`: String, id: String, status: Int)

  case class Id(value: String)
  case class Idx(name: String)
  case class Type(name: String)

  //document api
  case class Document(id: Id, data: Map[String, Any])

  object Index {
    def apply[A: EsDocument](index: Idx, `type`: Type, document: A, version: Option[Int] = None): Index = new Index(index, `type`, implicitly[EsDocument[A]].asDocument(document), version)
  }

  object Update {
    def apply[A: EsDocument](index: Idx, `type`: Type, document: A): Update = new Update(index, `type`, implicitly[EsDocument[A]].asDocument(document))
  }

  case class Index(index: Idx, `type`: Type, document: Document, version: Option[Int]) extends SingleDocumentApi
  case class Get(index: Idx, `type`: Type, id: Id) extends SingleDocumentApi
  case class Delete(index: Idx, `type`: Type, id: Id, version: Option[Int]) extends SingleDocumentApi
  case class Update(index: Idx, `type`: Type, document: Document) extends SingleDocumentApi
  case class MultiGet(ids: (Idx, Option[Type], Option[Id])*) extends DocumentApi

  case object BulkIndex extends BulkAction
  case object BulkCreate extends BulkAction
  case object BulkDelete extends BulkAction
  case object BulkUpdate extends BulkAction

  object Bulk {
    def apply(c: Index, create: Boolean = false): (BulkAction, SingleDocumentApi) = (if (create) BulkCreate else BulkIndex) → c
    def apply(c: Update): (BulkAction, SingleDocumentApi) = BulkUpdate → c
    def apply(c: Delete): (BulkAction, SingleDocumentApi) = BulkDelete → c
  }
  case class Bulk(actions: (BulkAction, SingleDocumentApi)*) extends DocumentApi

  case class BulkResponse(action: BulkAction, status: Int, response: Response)

  case class IndexResponse(created: Boolean, response: Response)

  //indices api
  case class DeleteIndex(index: Idx) extends IndicesApi
  case class DeleteIndexResponse(acknowledged: Boolean)

  object Refresh {
    def apply(index: Idx): Refresh = Refresh(Seq(index))
    def apply(): Refresh = Refresh(Seq.empty)
  }
  case class Refresh(indices: Seq[Idx]) extends IndicesApi
  case class RefreshResponse(shards: Shards)

  //search api
  case class QueryOptions(size: Option[Int] = None)
  sealed trait QueryLike extends Query {
    val options: QueryOptions
  }
  case class PrefixQuery(field: String, value: String, boost: Float = 0, options: QueryOptions = QueryOptions()) extends QueryLike

  object Search {
    def apply(index: Idx, `type`: Type, query: Query): Search = new Search(Seq(index), Seq(`type`), query)
    def apply(indices: Seq[Idx], `type`: Type, query: Query): Search = new Search(indices, Seq(`type`), query)
    def apply(index: Idx, types: Seq[Type], query: Query): Search = new Search(Seq(index), types, query)
  }
  case class Search(indices: Seq[Idx], types: Seq[Type], query: Query) extends SearchApi

  case class JsonDocument[Json](index: Idx, `type`: Type, id: Id, source: Json)
  case class SearchResponse[Json](shards: Shards, total: Int, documents: Seq[JsonDocument[Json]])

  case class StartScroll(index: Idx, `type`: Type, query: Query, ttl: FiniteDuration = 1 minute) extends SearchApi
  case class Scroll(scrollId: String, ttl: FiniteDuration = 1 minute) extends SearchApi

  object ClearScroll{
    def apply(scrollId: String): ClearScroll = ClearScroll(Set(scrollId))
  }
  case class ClearScroll(scrollIds: Set[String]) extends SearchApi

  case class ScrollResponse[Json](scrollId: String, results: SearchResponse[Json])
}
