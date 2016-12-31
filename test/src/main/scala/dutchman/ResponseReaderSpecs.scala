package dutchman

import dutchman.marshalling._
import dutchman.model.Person
import org.scalatest.{FlatSpec, Matchers}


abstract class ResponseReaderSpecs[Json](implicit reader: ResponseReader[Json]) extends FlatSpec with Matchers with JsonLoader {
  def getPerson(json: Json): Person

  "Hits" should "be decoded" in {

  }
}
