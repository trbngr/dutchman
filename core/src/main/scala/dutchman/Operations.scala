package dutchman

import dutchman.api._

import scala.concurrent.duration._

class Operations[Json] extends OperationDefinitions[Json, ESApi] {

  import cats.free.Free.liftF

  def bulk(actions: (BulkAction, SingleDocumentApi)*): ESApi[Seq[BulkResponse]] = {
    val api: Api[Seq[BulkResponse]] = Bulk(actions: _*)
    liftF(api)
  }

  def clearScroll(scrollIds: Set[String]): ESApi[Unit] = liftF(ClearScroll(scrollIds))

  def delete(index: Idx, `type`: Type, id: Id, version: Option[Int]): ESApi[DeleteResponse] =
    liftF(Delete(index, `type`, id, version))

  def deleteIndex(index: Idx): ESApi[DeleteIndexResponse] =
    liftF(DeleteIndex(index))

  def documentExists(index: Idx, `type`: Type, id: Id): ESApi[Boolean] =
    liftF(DocumentExists(index, `type`, id))

  def get(index: Idx, `type`: Type, id: Id): ESApi[GetResponse[Json]] = {
    val api: Api[GetResponse[Json]] = Get[Json](index, `type`, id)
    liftF(api)
  }

  def index[A: ESDocument](index: Idx, `type`: Type, document: A, version: Option[Int] = None): ESApi[IndexResponse] =
    liftF(Index(index, `type`, document, version))

  def multiGet(ids: (Idx, Option[Type], Option[Id])*): ESApi[MultiGetResponse] =
    liftF(MultiGet(ids: _*))

  def refresh(indices: Seq[Idx]): ESApi[RefreshResponse] =
    liftF(Refresh(indices))

  def refresh(index: Idx): ESApi[RefreshResponse] = liftF(Refresh(Seq(index)))

  def scroll(scrollId: String, ttl: FiniteDuration = 1 minute): ESApi[ScrollResponse[Json]] = {
    val api: Api[ScrollResponse[Json]] = Scroll(scrollId, ttl)
    liftF(api)
  }

  def startScroll(index: Idx, `type`: Type, query: Query, ttl: FiniteDuration = 1 minute, options: Option[SearchOptions]): ESApi[ScrollResponse[Json]] = {

    val api: Api[ScrollResponse[Json]] = StartScroll(index, `type`, query, ttl, options)
    liftF(api)
  }

  def update[A: ESDocument](index: Idx, `type`: Type, document: A): ESApi[UpdateResponse] =
    liftF(Update(index, `type`, document))

  def search(indices: Seq[Idx], types: Seq[Type], query: Query, options: Option[SearchOptions]) = {
    val api: Api[SearchResponse[Json]] = Search(indices, types, query, options)
    liftF(api)
  }

  def search(index: Idx, `type`: Type, query: Query, options: Option[SearchOptions]) = {
    val api: Api[SearchResponse[Json]] = Search(Seq(index), Seq(`type`), query, options)
    liftF(api)
  }

  def search(index: Idx, types: Seq[Type], query: Query, options: Option[SearchOptions]) = {
    val api: Api[SearchResponse[Json]] = Search(Seq(index), types, query, options)
    liftF(api)
  }

  def search(indices: Seq[Idx], `type`: Type, query: Query, options: Option[SearchOptions]) = {
    val api: Api[SearchResponse[Json]] = Search(indices, Seq(`type`), query, options)
    liftF(api)
  }

}

