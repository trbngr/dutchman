package dutchman.api.formatting

import dutchman.api._
import dutchman.http._

object document {

  private[formatting] object DocumentApiFormatter extends DataFormatter[DocumentApi] with RequestFormatter[DocumentApi] {
    def data(api: DocumentApi) = api match {
      case v: Get[_]         ⇒ Map.empty
      case v: Index          ⇒ v.document.data
      case v: Delete         ⇒ Map.empty
      case v: Update         ⇒ v.document.data
      case _: DocumentExists ⇒ Map.empty

      case v: MultiGet ⇒ Map("docs" → v.ids.map {
        case (index, Some(tpe), Some(id)) ⇒ Map("_index" → index.name, "_type" → tpe.name, "_id" → id.value)
        case (index, Some(tpe), None)     ⇒ Map("_index" → index.name, "_type" → tpe.name)
        case (index, _, _)                ⇒ Map("_index" → index.name)
      })

      case v: Bulk[_] ⇒
        val actions = v.actions.flatMap {
          case (action, bulkApi) ⇒
            val name = action match {
              case BulkCreate ⇒ "create"
              case BulkUpdate ⇒ "update"
              case BulkIndex  ⇒ "index"
              case BulkDelete ⇒ "delete"
            }

            bulkApi match {
              case x: Get[_] ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.id.value)), data(x))
              case x: Delete ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.id.value)), data(x))
              case x: Update ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.document.id.value)), Map("doc" → data(x)))
              case x: Index  ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.document.id.value)), data(x))
            }
        }
        //{"actions: [{"action": Map}, {doc}]}
        Map("actions" → actions)
    }

    def request(api: DocumentApi) = api match {
      case Get(index, tpe, id)            ⇒ Request(GET, s"/${index.name}/${tpe.name}/${id.value}")
      case op: Index                      ⇒ Request(PUT, s"/${op.index.name}/${op.`type`.name}/${op.document.id.value}", Map() ++ op.version.map(v ⇒ "version" → v.toString))
      case op: Delete                     ⇒ Request(DELETE, s"/${op.index.name}/${op.`type`.name}/${op.id.value}", Map() ++ op.version.map(v ⇒ "version" → v.toString))
      case Update(index, tpe, document)   ⇒ Request(PUT, s"/${index.name}/${tpe.name}/${document.id.value}")
      case _: MultiGet                    ⇒ Request(GET, "/_mget")
      case _: Bulk[_]                     ⇒ Request(POST, "/_bulk")
      case DocumentExists(index, tpe, id) ⇒ Request(HEAD, s"/${index.name}/${tpe.name}/${id.value}")
    }
  }
}
