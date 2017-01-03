package dutchman

import dutchman.dsl._
import dutchman.http._

package object api extends QueryApiSupport with SearchOptionsApiSupport {

  val BulkActionsKey = "bulk_actions"

  type ApiData = Map[String, Any]

  object ApiData {
    def empty = Map.empty[String, Any]
  }

  case class ApiRepresentation(request: Request, data: ApiData = ApiData.empty)

  def generateApi[A](op: ElasticOp[A]): ApiRepresentation = op match {
    case Bulk(actions) ⇒ ApiRepresentation(
      request = Request(POST, "/_bulk"),
      data = Map(BulkActionsKey → actions.flatMap {
        case (action, bulkApi) ⇒
          val name = action match {
            case BulkCreate ⇒ "create"
            case BulkUpdate ⇒ "update"
            case BulkIndex  ⇒ "index"
            case BulkDelete ⇒ "delete"
          }

          bulkApi match {
            case x: Get[_] ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.id.value)), x.data)
            case x: Delete ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.id.value)), x.data)
            case x: Update ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.document.id.value)), Map("doc" → x.data))
            case x: Index  ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.document.id.value)), x.data)
          }
      })
    )

    case DocumentExists(index, tpe, id) ⇒ ApiRepresentation(
      request = Request(HEAD, s"/${index.name}/${tpe.name}/${id.value}"),
      data = ApiData.empty
    )

    case Delete(index, tpe, id, version) ⇒ ApiRepresentation(
      request = Request(DELETE, s"/${index.name}/${tpe.name}/${id.value}", Map() ++ version.map(v ⇒ "version" → v.toString))
    )

    case Get(index, tpe, id) ⇒ ApiRepresentation(
      request = Request(GET, s"/${index.name}/${tpe.name}/${id.value}")
    )

    case Index(index, tpe, document, version) ⇒ ApiRepresentation(
      request = Request(PUT, s"/${index.name}/${tpe.name}/${document.id.value}", Map() ++ version.map(v ⇒ "version" → v.toString)),
      data = document.data
    )

    case MultiGet(ids) ⇒ ApiRepresentation(
      request = Request(GET, "/_mget"),
      data = Map("docs" → ids.map {
        case (index, Some(tpe), Some(id)) ⇒ Map("_index" → index.name, "_type" → tpe.name, "_id" → id.value)
        case (index, Some(tpe), None)     ⇒ Map("_index" → index.name, "_type" → tpe.name)
        case (index, _, _)                ⇒ Map("_index" → index.name)
      })
    )

    case Update(index, tpe, document) ⇒ ApiRepresentation(
      request = Request(PUT, s"/${index.name}/${tpe.name}/${document.id.value}"),
      data = document.data
    )

    case DeleteIndex(index) ⇒ ApiRepresentation(Request(DELETE, s"/${index.name}"))
    case Refresh(idx)       ⇒ ApiRepresentation(Request(POST, idx match {
      case indices if indices.isEmpty ⇒ "/_refresh"
      case indices                    ⇒ s"/${indices.map(_.name).mkString(",")}/_refresh"
    }))

    case Search(indices, types, query, options) ⇒
      val path = (indices, types) match {
        case (i, t) if i.isEmpty && t.isEmpty ⇒ "/_search"
        case (i, t) if t.isEmpty              ⇒ s"/${i.map(_.name).mkString(",")}/_search"
        case (i, t)                           ⇒ s"/${i.map(_.name).mkString(",")}/${t.map(_.name).mkString(",")}/_search"
      }
      val params = if (indices.size > 1) Map("ignore_unavailable" → "true") else Map.empty[String, String]
      val request = Request(POST, path, params)

      ApiRepresentation(
        request = request,
        data = Map("query" → query.data) ++ options.data
      )
  }

  implicit class ApiGenerator[A](op: ElasticOp[A]) {
    def api: ApiRepresentation = generateApi(op)
    def data: ApiData = api.data
  }
}
