package dutchman

import dutchman.api._

import scala.concurrent.duration._

trait OperationDefinitions[Json, Ret[_]] {
  def bulk(actions: (BulkAction, SingleDocumentApi)*): Ret[Seq[BulkResponse]]
  def clearScroll(scrollIds: Set[String]): Ret[Unit]
  def delete(index: Idx, `type`: Type, id: Id, version: Option[Int]): Ret[DeleteResponse]
  def deleteIndex(index: Idx): Ret[DeleteIndexResponse]
  def documentExists(index: Idx, `type`: Type, id: Id): Ret[Boolean]
  def get(index: Idx, `type`: Type, id: Id): Ret[GetResponse[Json]]
  def index[A: ESDocument](index: Idx, `type`: Type, document: A, version: Option[Int] = None): Ret[IndexResponse]
  def multiGet(ids: (Idx, Option[Type], Option[Id])*): Ret[MultiGetResponse]
  def refresh(indices: Seq[Idx]): Ret[RefreshResponse]
  def refresh(index: Idx): Ret[RefreshResponse]
  def scroll(scrollId: String, ttl: FiniteDuration = 1 minute): Ret[ScrollResponse[Json]]

  def search(index: Idx, `type`: Type, query: Query, options: Option[SearchOptions]): Ret[SearchResponse[Json]]
  def search(index: Idx, types: Seq[Type], query: Query, options: Option[SearchOptions]): Ret[SearchResponse[Json]]
  def search(indices: Seq[Idx], `type`: Type, query: Query, options: Option[SearchOptions]): Ret[SearchResponse[Json]]
  def search(indices: Seq[Idx], types: Seq[Type], query: Query, options: Option[SearchOptions]): Ret[SearchResponse[Json]]

  def startScroll(index: Idx, `type`: Type, query: Query, ttl: FiniteDuration = 1 minute, options: Option[SearchOptions]): Ret[ScrollResponse[Json]]
  def update[A: ESDocument](index: Idx, `type`: Type, document: A): Ret[UpdateResponse]
}
