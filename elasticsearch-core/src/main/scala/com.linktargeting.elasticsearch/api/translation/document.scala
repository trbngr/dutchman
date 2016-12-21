package com.linktargeting.elasticsearch.api.translation

import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.http._

trait document {

  private[translation] object DocumentApiTranslator extends DataTranslator[DocumentApi] with RequestTranslator[DocumentApi] {
    def data(api: DocumentApi) = api match {
      case v: Get      ⇒ GetTranslator.data(v)
      case v: Index    ⇒ IndexTranslator.data(v)
      case v: Delete   ⇒ DeleteTranslator.data(v)
      case v: Update   ⇒ UpdateTranslator.data(v)
      case v: MultiGet ⇒ MultiGetTranslator.data(v)
      case v: Bulk     ⇒ BulkTranslator.data(v)
    }
    def request(api: DocumentApi) = api match {
      case v: Get      ⇒ GetTranslator.request(v)
      case v: Index    ⇒ IndexTranslator.request(v)
      case v: Delete   ⇒ DeleteTranslator.request(v)
      case v: Update   ⇒ UpdateTranslator.request(v)
      case v: MultiGet ⇒ MultiGetTranslator.request(v)
      case v: Bulk     ⇒ BulkTranslator.request(v)
    }
  }

  private[translation] object IndexTranslator extends DataTranslator[Index] with RequestTranslator[Index] {
    def data(op: Index) = op.document.data
    def request(op: Index) = {
      Request(PUT, s"/${op.index.name}/${op.`type`.name}/${op.document.id.value}", Map() ++ op.version.map(v ⇒ "version" → v.toString))
    }
  }

  private[translation] object GetTranslator extends DataTranslator[Get] with RequestTranslator[Get] {
    def data(op: Get) = Map.empty
    def request(op: Get) = Request(GET, s"/${op.index.name}/${op.`type`.name}/${op.id.value}")
  }

  private[translation] object DeleteTranslator extends DataTranslator[Delete] with RequestTranslator[Delete] {
    def data(op: Delete) = Map.empty
    def request(op: Delete) = Request(DELETE, s"/${op.index.name}/${op.`type`.name}/${op.id.value}", Map() ++ op.version.map(v ⇒ "version" → v.toString))
  }

  private[translation] object UpdateTranslator extends DataTranslator[Update] with RequestTranslator[Update] {
    def data(op: Update) = op.document.data
    def request(op: Update) = Request(PUT, s"/${op.index.name}/${op.`type`.name}/${op.document.id.value}")
  }

  private[translation] object MultiGetTranslator extends DataTranslator[MultiGet] with RequestTranslator[MultiGet] {
    def data(op: MultiGet) = {
      Map("docs" → op.ids.map {
        case (index, Some(tpe), Some(id)) ⇒ Map("_index" → index.name, "_type" → tpe.name, "_id" → id.value)
        case (index, Some(tpe), None)     ⇒ Map("_index" → index.name, "_type" → tpe.name)
        case (index, _, _)                ⇒ Map("_index" → index.name)
      })
    }
    def request(op: MultiGet) = Request(GET, "/_mget")
  }

  private[translation] object SingleDocumentApiTranslator$ extends DataTranslator[SingleDocumentApi] {
    def data(op: SingleDocumentApi) = op match {
      case api: Get    ⇒ GetTranslator.data(api)
      case api: Delete ⇒ DeleteTranslator.data(api)
      case api: Update ⇒ UpdateTranslator.data(api)
      case api: Index  ⇒ IndexTranslator.data(api)
    }
  }

  private[translation] object BulkTranslator extends DataTranslator[Bulk] with RequestTranslator[Bulk] {
    def getAction(action: BulkAction) = action match {
      case BulkCreate ⇒ "create"
      case BulkUpdate ⇒ "update"
      case BulkIndex  ⇒ "index"
      case BulkDelete ⇒ "delete"
    }

    def data(op: Bulk) = {

      val actions: Seq[Map[String, Any]] = op.actions.flatMap {
        case (action, api) ⇒
          val name = getAction(action)
          api match {
            case x: Get    ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.id.value)), GetTranslator.data(x))
            case x: Delete ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.id.value)), DeleteTranslator.data(x))
            case x: Update ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.document.id.value)), Map("doc" → UpdateTranslator.data(x)))
            case x: Index  ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.document.id.value)), IndexTranslator.data(x))
          }
      }
      //{"actions: [{"action": Map}, {doc}]}
      Map("actions" → actions)
    }
    def request(op: Bulk) = Request(POST, "/_bulk")
  }
}
