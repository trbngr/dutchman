package com.linktargeting.elasticsearch.api.translation

import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.http._

trait document {

  private[translation] object DocumentApiMapper extends DataMapper[DocumentApi] with HttpBuilder[DocumentApi] {
    def data(api: DocumentApi) = api match {
      case v: Get      ⇒ GetMapper.data(v)
      case v: Index    ⇒ IndexMapper.data(v)
      case v: Delete   ⇒ DeleteMapper.data(v)
      case v: Update   ⇒ UpdateMapper.data(v)
      case v: MultiGet ⇒ MultiGetMapper.data(v)
      case v: Bulk     ⇒ BulkMapper.data(v)
    }
    def buildRequest(api: DocumentApi) = api match {
      case v: Get      ⇒ GetMapper.buildRequest(v)
      case v: Index    ⇒ IndexMapper.buildRequest(v)
      case v: Delete   ⇒ DeleteMapper.buildRequest(v)
      case v: Update   ⇒ UpdateMapper.buildRequest(v)
      case v: MultiGet ⇒ MultiGetMapper.buildRequest(v)
      case v: Bulk     ⇒ BulkMapper.buildRequest(v)
    }
  }

  private[translation] object IndexMapper extends DataMapper[Index] with HttpBuilder[Index] {
    def data(op: Index) = op.document.data
    def buildRequest(op: Index) = {
      Request(PUT, s"/${op.index.name}/${op.`type`.name}/${op.document.id.value}", Map() ++ op.version.map(v ⇒ "version" → v.toString))
    }
  }

  private[translation] object GetMapper extends DataMapper[Get] with HttpBuilder[Get] {
    def data(op: Get) = Map.empty
    def buildRequest(op: Get) = Request(GET, s"/${op.index.name}/${op.`type`.name}/${op.id.value}")
  }

  private[translation] object DeleteMapper extends DataMapper[Delete] with HttpBuilder[Delete] {
    def data(op: Delete) = Map.empty
    def buildRequest(op: Delete) = Request(DELETE, s"/${op.index.name}/${op.`type`.name}/${op.id.value}", Map() ++ op.version.map(v ⇒ "version" → v.toString))
  }

  private[translation] object UpdateMapper extends DataMapper[Update] with HttpBuilder[Update] {
    def data(op: Update) = op.document.data
    def buildRequest(op: Update) = Request(PUT, s"/${op.index.name}/${op.`type`.name}/${op.document.id.value}")
  }

  private[translation] object MultiGetMapper extends DataMapper[MultiGet] with HttpBuilder[MultiGet] {
    def data(op: MultiGet) = {
      Map("docs" → op.ids.map {
        case (index, Some(tpe), Some(id)) ⇒ Map("_index" → index.name, "_type" → tpe.name, "_id" → id.value)
        case (index, Some(tpe), None)     ⇒ Map("_index" → index.name, "_type" → tpe.name)
        case (index, _, _)                ⇒ Map("_index" → index.name)
      })
    }
    def buildRequest(op: MultiGet) = Request(GET, "/_mget")
  }

  private[translation] object SingleDocumentApiMapper extends DataMapper[SingleDocumentApi] {
    def data(op: SingleDocumentApi) = op match {
      case api: Get    ⇒ GetMapper.data(api)
      case api: Delete ⇒ DeleteMapper.data(api)
      case api: Update ⇒ UpdateMapper.data(api)
      case api: Index  ⇒ IndexMapper.data(api)
    }
  }

  private[translation] object BulkMapper extends DataMapper[Bulk] with HttpBuilder[Bulk] {
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
            case x: Get    ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.id.value)), GetMapper.data(x))
            case x: Delete ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.id.value)), DeleteMapper.data(x))
            case x: Update ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.document.id.value)), Map("doc" → UpdateMapper.data(x)))
            case x: Index  ⇒ Seq(Map(name → Map("_index" → x.index.name, "_type" → x.`type`.name, "_id" → x.document.id.value)), IndexMapper.data(x))
          }
      }
      //{"actions: [{"action": Map}, {doc}]}
      Map("actions" → actions)
    }
    def buildRequest(op: Bulk) = Request(POST, "/_bulk")
  }
}
