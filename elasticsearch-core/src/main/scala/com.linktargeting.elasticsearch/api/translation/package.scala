package com.linktargeting.elasticsearch.api

import com.linktargeting.elasticsearch.http.marshalling.ApiMarshaller

package object translation
  extends document
    with indices
    with search {

  import com.linktargeting.elasticsearch.http._

  def apiData(api: Api): Map[String, Any] = ApiMapper.data(api)
  def apiRequest(api: Api)(implicit marshaller: ApiMarshaller): Request = {
    ApiMapper.buildRequest(api).copy(
      payload = marshaller.stringify(api)
    )
  }

  private[this] implicit object ApiMapper extends DataMapper[Api] with HttpBuilder[Api] {
    def data(api: Api) = api match {
      case v: DocumentApi ⇒ DocumentApiMapper.data(v)
      case v: IndicesApi  ⇒ IndicesApiMapper.data(v)
      case v: SearchApi   ⇒ SearchApiMapper.data(v)
    }

    def buildRequest(api: Api) = api match {
      case v: DocumentApi ⇒ DocumentApiMapper.buildRequest(v)
      case v: IndicesApi  ⇒ IndicesApiMapper.buildRequest(v)
      case v: SearchApi   ⇒ SearchApiMapper.buildRequest(v)
    }
  }

  trait DataMapper[A] {
    def data(o: A): Map[String, Any]
  }

  trait HttpBuilder[A <: Api] {
    def buildRequest(op: A): Request
  }

  trait EsDocument[A] {
    implicit def asDocument(a: A): Document
  }

}
