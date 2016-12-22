package com.linktargeting.elasticsearch.http

import com.linktargeting.elasticsearch.api._

object marshalling {
  case class DecodingError(message: String) extends Exception(s"Error decoding json: $message")

  trait ApiMarshaller {
    def stringify(api: Api): String
  }

  trait ApiUnMarshaller[Json] {
    def read(json: String): Json
    def error(json: String): ESError
    def readError(json: Json): Option[Seq[ESError]]

    def index(json: Json): IndexResponse
    def deleteIndex(json: Json): DeleteIndexResponse
    def bulk(json: Json): Seq[BulkResponse]
    def search(json: Json): SearchResponse[Json]
    def refresh(json: Json): RefreshResponse
    def scroll(json: Json): ScrollResponse[Json]
  }

  object syntax {

    implicit class UnMarshaller[Json](json: Json)(implicit unMarshaller: ApiUnMarshaller[Json]) {
      def readError = unMarshaller.readError(json)
    }

    implicit class jsonToError[Json](json: String)(implicit unMarshaller: ApiUnMarshaller[Json]) {
      def error = unMarshaller.error(json)
      def parseJson = unMarshaller.read(json)
    }

    implicit class IndexResponseSyntax[Json](api: Index)(implicit u: ApiUnMarshaller[Json]) {
      def response(json: Json) = u.index(json)
    }
    implicit class DeleteIndexResponseSyntax[Json](api: DeleteIndex)(implicit u: ApiUnMarshaller[Json]) {
      def response(json: Json) = u.deleteIndex(json)
    }
    implicit class BulkResponseSyntax[Json](api: Bulk)(implicit u: ApiUnMarshaller[Json]) {
      def response(json: Json) = {
        println(s"bulk: $json")
        u.bulk(json)
      }
    }
    implicit class SearchResponseSyntax[Json](api: Search)(implicit u: ApiUnMarshaller[Json]) {
      def response(json: Json) = u.search(json)
    }
    implicit class RefreshResponseSyntax[Json](api: Refresh)(implicit u: ApiUnMarshaller[Json]) {
      def response(json: Json) = u.refresh(json)
    }
    implicit class StartScrollResponseSyntax[Json](api: StartScroll)(implicit u: ApiUnMarshaller[Json]) {
      def response(json: Json) = {
        println(s"StartScrollResponseSyntax: $json")
        u.scroll(json)
      }
    }
    implicit class ScrollResponseSyntax[Json](api: Scroll)(implicit u: ApiUnMarshaller[Json]) {
      def response(json: Json) = u.scroll(json)
    }

  }
}
