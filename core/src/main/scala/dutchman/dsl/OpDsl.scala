package dutchman.dsl

import dutchman.ElasticResponse

trait OpDsl {

  //Bulk only supports single document operations. This is nothing but a marker
  sealed trait BulkActionable
  sealed trait ElasticOp[A]

  sealed trait BulkAction
  case object BulkIndex extends BulkAction
  case object BulkCreate extends BulkAction
  case object BulkDelete extends BulkAction
  case object BulkUpdate extends BulkAction

  object BulkAction {
    def apply(c: Index, create: Boolean = false): (BulkAction, Index) = (if (create) BulkCreate else BulkIndex) → c
    def apply[A](c: Update): (BulkAction, Update) = BulkUpdate → c
    def apply(c: Delete): (BulkAction, Delete) = BulkDelete → c
  }

  case class DocumentExists(index: Idx, `type`: Type, id: Id) extends ElasticOp[Boolean]

  case class Get[Json](index: Idx, `type`: Type, id: Id) extends ElasticOp[ElasticResponse[GetResponse[Json]]]
  case class GetResponse[Json](index: String, `type`: String, id: String, version: Int, found: Boolean, source: Json)

  case class Delete(index: Idx, `type`: Type, id: Id, version: Option[Int]) extends ElasticOp[ElasticResponse[DeleteResponse]] with BulkActionable
  case class DeleteResponse()

  object Index {
    def apply[D: ESDocument](index: Idx, `type`: Type, document: D, version: Option[Int]): Index = new Index(index, `type`, implicitly[ESDocument[D]].document(document), version)
  }
  case class Index(index: Idx, `type`: Type, document: ElasticDocument, version: Option[Int]) extends ElasticOp[ElasticResponse[IndexResponse]] with BulkActionable
  case class IndexResponse(created: Boolean, response: Response)

  object Update {
    def apply[D: ESDocument](index: Idx, `type`: Type, document: D): Update = new Update(index, `type`, implicitly[ESDocument[D]].document(document))
  }
  case class Update(index: Idx, `type`: Type, document: ElasticDocument) extends ElasticOp[ElasticResponse[UpdateResponse]] with BulkActionable
  case class UpdateResponse()

  case class MultiGet[Json](ids: Seq[(Idx, Option[Type], Option[Id])]) extends ElasticOp[ElasticResponse[MultiGetResponse[Json]]]
  case class MultiGetResponse[Json]()

  case class Bulk(actions: Seq[(BulkAction, BulkActionable)]) extends ElasticOp[ElasticResponse[Seq[BulkResponse]]]
  case class BulkResponse(action: BulkAction, status: Int, response: Response)

  case class DeleteIndex(index: Idx) extends ElasticOp[ElasticResponse[DeleteIndexResponse]]
  case class DeleteIndexResponse(acknowledged: Boolean)

  case class Refresh(indices: Seq[Idx]) extends ElasticOp[ElasticResponse[RefreshResponse]]
  case class RefreshResponse(shards: Shards)

  case class JsonDocument[Json](index: Idx, `type`: Type, id: Id, score: Float, source: Json)
  case class SearchResponse[Json](shards: Shards, total: Int, documents: Seq[JsonDocument[Json]])

  case class Search[Json](indices: Set[Idx], types: Set[Type], query: Query, options: Option[SearchOptions]) extends ElasticOp[ElasticResponse[SearchResponse[Json]]]

}
