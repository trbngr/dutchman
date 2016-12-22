package com.linktargeting.elasticsearch.api

import com.linktargeting.elasticsearch.dsl.Dsl

import scala.concurrent.{ExecutionContext, Future}

trait syntax {

  class Syntax[Json, A <: Api, B](f: ⇒ Future[B])(implicit ec: ExecutionContext) {
    def apply() = f
    def apply[C](fx: B ⇒ C) = f map fx
    def map[C](m: B ⇒ C) = f map m
  }

  implicit class IndexSyntax[Json](api: Index)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, Index, IndexResponse](client.document(api))

  implicit class BulkSyntax[Json](api: Bulk)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, Bulk, Seq[BulkResponse]](client.document(api))

  implicit class DeleteIndexSyntax[Json](api: DeleteIndex)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, DeleteIndex, DeleteIndexResponse](client.indices(api))

  implicit class RefreshSyntax[Json](api: Refresh)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, Refresh, RefreshResponse](client.indices(api))

  implicit class SearchSyntax[Json](api: Search)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, Search, SearchResponse[Json]](client.search(api))

  implicit class ScrollSyntax[Json](api: Scroll)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, Scroll, ScrollResponse[Json]](client.search(api))

  implicit class StartScrollSyntax[Json](api: StartScroll)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, StartScroll, ScrollResponse[Json]](client.search(api))

  implicit class ClearScrollSyntax[Json](api: ClearScroll)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, ClearScroll, Unit](client.search(api))

}
