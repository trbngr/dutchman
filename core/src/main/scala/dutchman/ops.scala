package dutchman

object ops {

  import cats.free.Free.liftF
  import dutchman.dsl._

  def multiGet[Json](ids: (Idx, Option[Type], Option[Id])*): ElasticOps[ElasticResponse[MultiGetResponse[Json]]] = {
    val op: ElasticOp[ElasticResponse[MultiGetResponse[Json]]] = MultiGet[Json](ids)
    liftF(op)
  }

  def get[Json](index: Idx, `type`: Type, id: Id): ElasticOps[ElasticResponse[GetResponse[Json]]] = {
    val g: ElasticOp[ElasticResponse[GetResponse[Json]]] = Get[Json](index, `type`, id)
    liftF(g)
  }

  def delete(index: Idx, `type`: Type, id: Id, version: Option[Int]): ElasticOps[ElasticResponse[DeleteResponse]] = liftF(Delete(index, `type`, id, version))

  def index[D: ESDocument](index: Idx, `type`: Type, document: D, version: Option[Int]): ElasticOps[ElasticResponse[IndexResponse]] = {
    val doc = implicitly[ESDocument[D]].document(document)
    liftF(Index(index, `type`, doc, version))
  }

  def index(index: Idx, `type`: Type, document: ElasticDocument, version: Option[Int]): ElasticOps[ElasticResponse[IndexResponse]] =
    liftF(Index(index, `type`, document, version))

  def update[D: ESDocument](index: Idx, `type`: Type, document: D): ElasticOps[ElasticResponse[UpdateResponse]] = {
    val doc = implicitly[ESDocument[D]].document(document)
    liftF(Update(index, `type`, doc))
  }
  def update(index: Idx, `type`: Type, document: ElasticDocument): ElasticOps[ElasticResponse[UpdateResponse]] =
    liftF(Update(index, `type`, document))

  def documentExists(index: Idx, `type`: Type, id: Id): ElasticOps[Boolean] =
    liftF(DocumentExists(index, `type`, id))

  def deleteIndex(index: Idx): ElasticOps[ElasticResponse[DeleteIndexResponse]] = liftF(DeleteIndex(index))

  def refresh(indices: Idx*): ElasticOps[ElasticResponse[RefreshResponse]] = liftF(Refresh(indices))

  def search[Json](index: Idx, `type`: Type, query: Query, options: Option[SearchOptions]): ElasticOps[ElasticResponse[SearchResponse[Json]]] =
    search[Json](Set(index), Set(`type`), query, options)

  def search[Json](index: Idx, types: Set[Type], query: Query, options: Option[SearchOptions]): ElasticOps[ElasticResponse[SearchResponse[Json]]] =
    search[Json](Set(index), types, query, options)

  def search[Json](indices: Set[Idx], `type`: Type, query: Query, options: Option[SearchOptions]): ElasticOps[ElasticResponse[SearchResponse[Json]]] =
    search[Json](indices, Set(`type`), query, options)

  def search[Json](indices: Set[Idx], types: Set[Type], query: Query, options: Option[SearchOptions]): ElasticOps[ElasticResponse[SearchResponse[Json]]] = {
    val api: ElasticOp[ElasticResponse[SearchResponse[Json]]] = Search[Json](indices, types, query, options)
    liftF(api)
  }

  def bulk(actions: (BulkAction, BulkActionable)*): ElasticOps[ElasticResponse[Seq[BulkResponse]]] = liftF(Bulk(actions))
}
