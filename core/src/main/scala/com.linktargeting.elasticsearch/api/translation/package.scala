package com.linktargeting.elasticsearch.api

import com.linktargeting.elasticsearch._
import com.linktargeting.elasticsearch.marshalling.ApiMarshaller

package object translation {

  import document.DocumentApiTranslator
  import indices.IndicesApiTranslator
  import search.SearchApiTranslator

  type DataContainer = Map[String, Any]
  type BulkContainers = Seq[DataContainer]

  def apiData(api: Api): DataContainer = ApiTranslator.data(api)

  def apiRequest(api: Api)(implicit marshaller: ApiMarshaller): Request = {
    ApiTranslator.request(api).copy(
      payload = marshaller.stringify(api)
    )
  }

  private[this] implicit object ApiTranslator extends DataTranslator[Api] with RequestTranslator[Api] {
    def data(api: Api) = api match {
      case v: DocumentApi ⇒ DocumentApiTranslator.data(v)
      case v: IndicesApi  ⇒ IndicesApiTranslator.data(v)
      case v: SearchApi   ⇒ SearchApiTranslator.data(v)
    }

    def request(api: Api) = api match {
      case v: DocumentApi ⇒ DocumentApiTranslator.request(v)
      case v: IndicesApi  ⇒ IndicesApiTranslator.request(v)
      case v: SearchApi   ⇒ SearchApiTranslator.request(v)
    }
  }

  trait DataTranslator[A] {
    def data(o: A): DataContainer
  }

  trait RequestTranslator[A <: Api] {
    def request(op: A): Request
  }

  trait ESDocument[A] {
    def document(a: A): Document
  }

}
