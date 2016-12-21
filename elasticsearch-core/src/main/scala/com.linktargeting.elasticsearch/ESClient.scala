package com.linktargeting.elasticsearch

import com.linktargeting.elasticsearch.api._

import scala.concurrent.Future

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
