package com.linktargeting.elasticsearch.api

import com.linktargeting.elasticsearch.http._
import com.linktargeting.elasticsearch.http.marshalling.ApiMarshaller

package object translation {

  import document.DocumentApiTranslator
  import indices.IndicesApiTranslator
  import search.SearchApiTranslator

  def apiData(api: Api): Map[String, Any] = ApiTranslator.data(api)
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
    def data(o: A): Map[String, Any]
  }

  trait RequestTranslator[A <: Api] {
    def request(op: A): Request
  }

  trait EsDocument[A] {
    implicit def asDocument(a: A): Document
  }

}
