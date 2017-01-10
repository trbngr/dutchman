package dutchman

import cats.data.EitherT

object ops {

  import cats.free.Free.liftF
  import dutchman.dsl._

  def multiGet[Json](ids: (Idx, Option[Type], Option[Id])*): ElasticResponse[MultiGetResponse[Json]] =
    EitherT[ElasticOps, ESError, MultiGetResponse[Json]] {
      liftF[ElasticOp, Result[MultiGetResponse[Json]]](MultiGet[Json](ids))
    }

  def get[Json](index: Idx, `type`: Type, id: Id): ElasticResponse[GetResponse[Json]] =
    EitherT[ElasticOps, ESError, GetResponse[Json]] {
      liftF[ElasticOp, Result[GetResponse[Json]]](Get[Json](index, `type`, id))
    }

  def delete(index: Idx, `type`: Type, id: Id, version: Option[Int]): ElasticResponse[DeleteResponse] =
    EitherT[ElasticOps, ESError, DeleteResponse](liftF(Delete(index, `type`, id, version)))

  def index[D: ESDocument](index: Idx, `type`: Type, document: D, version: Option[Int]): ElasticResponse[IndexResponse] = {
    EitherT[ElasticOps, ESError, IndexResponse] {
      val doc = implicitly[ESDocument[D]].document(document)
      liftF(Index(index, `type`, doc, version))
    }
  }

  def index(index: Idx, `type`: Type, document: ElasticDocument, version: Option[Int]): ElasticResponse[IndexResponse] =
    EitherT[ElasticOps, ESError, IndexResponse] {
      liftF(Index(index, `type`, document, version))
    }

  def update[D: ESDocument](index: Idx, `type`: Type, document: D): ElasticResponse[UpdateResponse] =
    EitherT[ElasticOps, ESError, UpdateResponse] {
      val doc = implicitly[ESDocument[D]].document(document)
      liftF(Update(index, `type`, doc))
    }

  def update(index: Idx, `type`: Type, document: ElasticDocument): ElasticResponse[UpdateResponse] =
    EitherT[ElasticOps, ESError, UpdateResponse] {
      liftF(Update(index, `type`, document))
    }

  def documentExists(index: Idx, `type`: Type, id: Id): ElasticOps[Boolean] =
    liftF(DocumentExists(index, `type`, id))

  def deleteIndex(index: Idx): ElasticResponse[DeleteIndexResponse] =
    EitherT[ElasticOps, ESError, DeleteIndexResponse]{
      liftF(DeleteIndex(index))
    }

  def refresh(indices: Idx*): ElasticResponse[RefreshResponse] =
    EitherT[ElasticOps, ESError, RefreshResponse]{
      liftF(Refresh(indices))
    }

  def search[Json](index: Idx, `type`: Type, query: Query, options: Option[SearchOptions]): ElasticResponse[SearchResponse[Json]] =
    search[Json](Set(index), Set(`type`), query, options)

  def search[Json](index: Idx, types: Set[Type], query: Query, options: Option[SearchOptions]): ElasticResponse[SearchResponse[Json]] =
    search[Json](Set(index), types, query, options)

  def search[Json](indices: Set[Idx], `type`: Type, query: Query, options: Option[SearchOptions]): ElasticResponse[SearchResponse[Json]] =
    search[Json](indices, Set(`type`), query, options)

  def search[Json](indices: Set[Idx], types: Set[Type], query: Query, options: Option[SearchOptions]): ElasticResponse[SearchResponse[Json]] = {
    EitherT[ElasticOps, ESError, SearchResponse[Json]]{
      liftF[ElasticOp, Result[SearchResponse[Json]]](Search[Json](indices, types, query, options))
    }
  }

  def bulk(actions: (BulkAction, BulkActionable)*): ElasticResponse[Seq[BulkResponse]] =
    EitherT[ElasticOps, ESError, Seq[BulkResponse]]{
      liftF(Bulk(actions))
    }

  implicit class OpsToEitherT[A](ops: ElasticOps[A]){
    def either[E, B](f: A ⇒ Either[E, B]): EitherT[ElasticOps, E, B] = EitherT[ElasticOps, E, B](ops map f)
    def right: EitherT[ElasticOps, ESError, A] = EitherT.right(ops)
  }
}
