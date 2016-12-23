package com.linktargeting.elasticsearch.api

import com.linktargeting.elasticsearch.dsl.Dsl

import scala.concurrent.{ExecutionContext, Future}

trait syntax extends querySyntax {

  class Syntax[Json, Response](f: ⇒ Future[Response])(implicit ec: ExecutionContext) {
    def apply(): Future[Response] = f

    def task: Future[Response] = f

    def map[That](fx: Response ⇒ That): Future[That] = f map fx

    def flatMap[That](fx: Response ⇒ Future[That]): Future[That] = f flatMap fx
  }

  trait QueryOptionProvider[A <: SearchApiWithOptions] {
    def withOptions(options: SearchOptions): A
  }

  implicit class IndexSyntax[Json](api: Index)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, IndexResponse](client.document(api))

  implicit class BulkSyntax[Json](api: Bulk)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, Seq[BulkResponse]](client.document(api))

  implicit class DeleteIndexSyntax[Json](api: DeleteIndex)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, DeleteIndexResponse](client.indices(api))

  implicit class RefreshSyntax[Json](api: Refresh)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, RefreshResponse](client.indices(api))

  implicit class SearchSyntax[Json](api: Search)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, SearchResponse[Json]](client.search(api)) with QueryOptionProvider[Search] {
    def withOptions(options: SearchOptions) = api.copy(
      query = QueryWithOptions(api.query, options)
    )
  }

  implicit class StartScrollSyntax[Json](api: StartScroll)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, ScrollResponse[Json]](client.search(api)) with QueryOptionProvider[StartScroll] {
    def withOptions(options: SearchOptions) = api.copy(
      query = QueryWithOptions(api.query, options)
    )
  }

  implicit class ScrollSyntax[Json](api: Scroll)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, ScrollResponse[Json]](client.search(api))

  implicit class ClearScrollSyntax[Json](api: ClearScroll)(implicit client: Dsl[Json], ec: ExecutionContext)
    extends Syntax[Json, Unit](client.search(api))

}

trait querySyntax {
}
