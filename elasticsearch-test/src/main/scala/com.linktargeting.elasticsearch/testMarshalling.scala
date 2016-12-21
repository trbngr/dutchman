package com.linktargeting.elasticsearch

import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.http.marshalling._

object testMarshalling extends testMarshalling

trait testMarshalling {
  implicit object marshaller extends ApiMarshaller {
    override def stringify(api: Api) = ""
  }

  implicit object unMarshaller extends ApiUnMarshaller[String]{
    override def read(json: String) = json
    override def error(json: String) = EsError("" , "", "", 500)
    override def index(json: String) = ???
    override def deleteIndex(json: String) = ???
    override def readError(json: String) = None
    override def bulk(json: String) = ???
    override def search(json: String) = ???
    override def refresh(json: String) = ???
    override def scroll(json: String) = ???
  }
}
