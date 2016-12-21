package com.linktargeting.elasticsearch

import com.linktargeting.elasticsearch.http.marshalling._
import com.linktargeting.elasticsearch.model._
import org.scalatest.{FlatSpec, Matchers}


abstract class ApiUnMarshallerSpecs[Json](implicit unMarshaller: ApiUnMarshaller[Json]) extends FlatSpec with Matchers with JsonLoader {
  def getPerson(json: Json): Person

  "Hits" should "be decoded" in {

  }
}
