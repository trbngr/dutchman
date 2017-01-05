package dutchman

object ops {

  import cats.free.Free.liftF
  import dutchman.dsl._

  def multiGet[Json](ids: (Idx, Option[Type], Option[Id])*): ElasticOps[MultiGetResponse[Json]] = {
    val op: ElasticOp[MultiGetResponse[Json]] = MultiGet[Json](ids)
    liftF(op)
  }

  def get[Json](index: Idx, `type`: Type, id: Id): ElasticOps[GetResponse[Json]] = {
    val g: ElasticOp[GetResponse[Json]] = Get[Json](index, `type`, id)
    liftF(g)
  }

  def delete(index: Idx, `type`: Type, id: Id, version: Option[Int]): ElasticOps[DeleteResponse] = liftF(Delete(index, `type`, id, version))

  def index[D: ESDocument](index: Idx, `type`: Type, document: D, version: Option[Int]): ElasticOps[IndexResponse] = {
    val doc = implicitly[ESDocument[D]].document(document)
    liftF(Index(index, `type`, doc, version))
  }

  def index(index: Idx, `type`: Type, document: ElasticDocument, version: Option[Int]): ElasticOps[IndexResponse] =
    liftF(Index(index, `type`, document, version))

  def update[D: ESDocument](index: Idx, `type`: Type, document: D) = {
    val doc = implicitly[ESDocument[D]].document(document)
    liftF(Update(index, `type`, doc))
  }
  def update(index: Idx, `type`: Type, document: ElasticDocument) =
    liftF(Update(index, `type`, document))

  def documentExists(index: Idx, `type`: Type, id: Id): ElasticOps[Boolean] =
    liftF(DocumentExists(index, `type`, id))

  def deleteIndex(index: Idx): ElasticOps[DeleteIndexResponse] = liftF(DeleteIndex(index))

  def refresh(indices: Idx*): ElasticOps[RefreshResponse] = liftF(Refresh(indices))

  def search[Json](index: Idx, `type`: Type, query: Query, options: Option[SearchOptions]): ElasticOps[SearchResponse[Json]] =
    search[Json](Seq(index), Seq(`type`), query, options)

  def search[Json](index: Idx, types: Seq[Type], query: Query, options: Option[SearchOptions]): ElasticOps[SearchResponse[Json]] =
    search[Json](Seq(index), types, query, options)

  def search[Json](indices: Seq[Idx], `type`: Type, query: Query, options: Option[SearchOptions]): ElasticOps[SearchResponse[Json]] =
    search[Json](indices, Seq(`type`), query, options)

  def search[Json](indices: Seq[Idx], types: Seq[Type], query: Query, options: Option[SearchOptions]): ElasticOps[SearchResponse[Json]] = {
    val api: ElasticOp[SearchResponse[Json]] = Search[Json](indices, types, query, options)
    liftF(api)
  }

  def bulk(actions: (BulkAction, BulkActionable)*): ElasticOps[Seq[BulkResponse]] = liftF(Bulk(actions))
}
