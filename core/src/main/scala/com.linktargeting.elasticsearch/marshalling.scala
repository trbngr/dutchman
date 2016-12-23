package com.linktargeting.elasticsearch

import com.linktargeting.elasticsearch.api._

object marshalling {
  case class DecodingError(message: String) extends Exception(s"Error decoding json: $message")

  trait ApiMarshaller {
    def marshal(api: Api): String
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
}
