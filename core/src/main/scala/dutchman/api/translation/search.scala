package dutchman.api.translation

import dutchman._
import dutchman.api._

object search {

  import query.QueryTranslator

  private[translation] object SearchApiTranslator extends DataTranslator[SearchApi] with RequestTranslator[SearchApi] {

    def data(api: SearchApi) = api match {
      case Search(_, _, query)         ⇒ Map("query" → QueryTranslator.data(query)) ++ SearchOptionsTranslator.data(query)
      case StartScroll(_, _, query, _) ⇒ Map("query" → (QueryTranslator.data(query) + ("sort" → Seq("_doc")))) ++ SearchOptionsTranslator.data(query)
      case x: Scroll                   ⇒ Map("sort" → Seq("_doc"))
      case ClearScroll(scrollIds)      ⇒ Map("scroll_id" → scrollIds)
    }

    def request(api: SearchApi) = api match {

      case x: Search ⇒
        val path = x match {
          case Search(indices, types, _) if indices.isEmpty && types.isEmpty ⇒ "/_search"
          case Search(indices, types, _) if types.isEmpty                    ⇒ s"/${indices.map(_.name).mkString(",")}/_search"
          case Search(indices, types, _)                                     ⇒ s"/${indices.map(_.name).mkString(",")}/${types.map(_.name).mkString(",")}/_search"
        }
        val params = if (x.indices.size > 1) Map("ignore_unavailable" → "true") else Map.empty[String, String]
        Request(POST, path, params)

      case x: StartScroll ⇒ x match {
        case StartScroll(index, tpe, _, ttl) ⇒
          Request(POST,
            path = s"/${index.name}/${tpe.name}/_search",
            params = Map("scroll" → s"${ttl.toSeconds}s")
          )
      }

      case x: Scroll ⇒ x match {
        case Scroll(id, ttl) ⇒
          println(s"scroll_id: $id")
          Request(GET,
            path = s"/_search",
            params = Map("scroll_id" → id, "scroll" → s"${ttl.toSeconds}s")
          )
      }

      case x: ClearScroll ⇒ Request(DELETE, path = s"/_search/scroll")
    }
  }

  private[translation] object SearchOptionsTranslator extends DataTranslator[Query] {
    override def data(query: Query) = query match {
      case QueryWithOptions(_, options) ⇒ Map.empty[String, Any] ++
        options.from.map("from" → _) ++
        options.size.map("size" → _) ++
        Map("sort" → options.sorters.map(sortData))
      case _                            ⇒ Map.empty
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
