package com.linktargeting.elasticsearch.api.translation

import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.http._

object document {

  object DocumentApiTranslator extends DataTranslator[DocumentApi] with RequestTranslator[DocumentApi] {
    def data(api: DocumentApi) = api match {
      case v: Get      ⇒ Map.empty
      case v: Index    ⇒ v.document.data
      case v: Delete   ⇒ Map.empty
      case v: Update   ⇒ v.document.data
      case v: MultiGet ⇒ Map("docs" → v.ids.map {
        case (index, Some(tpe), Some(id)) ⇒ Map("_index" → index.name, "_type" → tpe.name, "_id" → id.value)
        case (index, Some(tpe), None)     ⇒ Map("_index" → index.name, "_type" → tpe.name)
        case (index, _, _)                ⇒ Map("_index" → index.name)
      })
      case v: Bulk     ⇒
        val actions: Seq[Map[String, Any]] = v.actions.flatMap {
          case (action, bulkApi) ⇒
            def getAction(action: BulkAction) = action match {
              case BulkCreate ⇒ "create"
              case BulkUpdate ⇒ "update"
              case BulkIndex  ⇒ "index"
              case BulkDelete ⇒ "delete"
            }
            val name = getAction(action)
            bulkApi match {
              case x: Get    ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.id.value)), data(x))
              case x: Delete ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.id.value)), data(x))
              case x: Update ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.document.id.value)), Map("doc" → data(x)))
              case x: Index  ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.document.id.value)), data(x))
            }
        }
        //{"actions: [{"action": Map}, {doc}]}
        Map("actions" → actions)
    }

    def request(api: DocumentApi) = api match {
      case op: Get     ⇒ Request(GET, s"/${op.index.name}/${op.`type`.name}/${op.id.value}")
      case op: Index   ⇒ Request(PUT, s"/${op.index.name}/${op.`type`.name}/${op.document.id.value}", Map() ++ op.version.map(v ⇒ "version" → v.toString))
      case op: Delete  ⇒ Request(DELETE, s"/${op.index.name}/${op.`type`.name}/${op.id.value}", Map() ++ op.version.map(v ⇒ "version" → v.toString))
      case op: Update  ⇒ Request(PUT, s"/${op.index.name}/${op.`type`.name}/${op.document.id.value}")
      case _: MultiGet ⇒ Request(GET, "/_mget")
      case _: Bulk     ⇒ Request(POST, "/_bulk")
    }
  }
}
