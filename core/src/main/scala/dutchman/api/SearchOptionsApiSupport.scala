package dutchman.api

trait SearchOptionsApiSupport {

  import dutchman.dsl._

  implicit class SearchOptionsData(options: Option[SearchOptions]) {

    def data: ApiData = options.fold(ApiData.empty) { options ⇒
      Map.empty[String, Any] ++
        options.from.map("from" → _) ++
        options.size.map("size" → _) ++
        Map("sort" → options.sorters.map(sortData))
    }

    def sortData(sort: Sort): Any = sort match {
      case DocSort                                  ⇒ "_doc"
      case ScoreSort                                ⇒ "_score"
      case FieldSort(name, None, None)              ⇒ name
      case FieldSort(name, Some(order), None)       ⇒ Map(name → sortOrder(order))
      case FieldSort(name, None, Some(mode))        ⇒ Map(name → Map("mode" → sortMode(mode)))
      case FieldSort(name, Some(order), Some(mode)) ⇒ Map(name → Map(
        "order" → sortOrder(order),
        "mode" → sortMode(mode)
      ))
    }

    def sortMode(mode: SortMode) = mode match {
      case Min ⇒ "min"
      case Max ⇒ "max"
      case Sum ⇒ "sum"
      case Avg ⇒ "avg"
    }

    def sortOrder(order: SortOrder) = order match {
      case Asc  ⇒ "asc"
      case Desc ⇒ "desc"
    }
  }
}
