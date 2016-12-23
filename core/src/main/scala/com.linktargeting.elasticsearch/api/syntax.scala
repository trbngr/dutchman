package com.linktargeting.elasticsearch.api

import com.linktargeting.elasticsearch.dsl.Dsl

import scala.concurrent.{ExecutionContext, Future}

trait syntax extends querySyntax{

  class Syntax[Json, A <: Api, B](f: ⇒ Future[B])(implicit ec: ExecutionContext) {
    def apply() = f
    def task = f
    def map[C](fx: B ⇒ C) = f map fx
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
    extends Syntax[Json, Search, SearchResponse[Json]](client.search(api)) {
    def options[C](options: QueryOptions)(fx: SearchResponse[Json] ⇒ C) = client.search(api, options)
  }

  implicit class ScrollSyntax[Json](api: Scroll)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, Scroll, ScrollResponse[Json]](client.search(api))

  implicit class StartScrollSyntax[Json](api: StartScroll)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, StartScroll, ScrollResponse[Json]](client.search(api)) {
    def options[C](options: QueryOptions)(fx: ScrollResponse[Json] ⇒ C) = client.search(api, options)
  }

  implicit class ClearScrollSyntax[Json](api: ClearScroll)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, ClearScroll, Unit](client.search(api))

}

trait querySyntax {
}
