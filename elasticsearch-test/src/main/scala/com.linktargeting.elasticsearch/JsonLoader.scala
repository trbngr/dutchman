package com.linktargeting.elasticsearch

import com.linktargeting.elasticsearch.http.marshalling.ApiUnMarshaller

import scala.io.Source

trait JsonLoader {

  def loadJson[Json](f: String)(implicit unmarshaller: ApiUnMarshaller[Json]) = {
    val stream = getClass.getResourceAsStream(s"/$f.json")
    unmarshaller.read(Source.fromInputStream(stream).mkString)
  }
}
