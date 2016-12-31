package dutchman

import marshalling.ResponseReader

import scala.io.Source

trait JsonLoader {

  def loadJson[Json](f: String)(implicit reader: ResponseReader[Json]) = {
    val stream = getClass.getResourceAsStream(s"/$f.json")
    reader.read(Source.fromInputStream(stream).mkString)
  }
}
