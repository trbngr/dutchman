package com.linktargeting.elasticsearch

import com.linktargeting.elasticsearch.http.marshalling.ApiUnMarshaller

import scala.io.Source

trait JsonLoader {
  import com.linktargeting.elasticsearch.http.marshalling.syntax._

  def loadJson[Json](f: String)(implicit unmarshaller: ApiUnMarshaller[Json]) = {
    val stream = getClass.getResourceAsStream(s"/$f.json")
    Source.fromInputStream(stream).mkString parseJson
  }
}
