package dutchman

import marshalling._
import dutchman.model._
import dutchman.model.Person
import org.scalatest.{FlatSpec, Matchers}


abstract class ApiUnMarshallerSpecs[Json](implicit unMarshaller: ApiUnMarshaller[Json]) extends FlatSpec with Matchers with JsonLoader {
  def getPerson(json: Json): Person

  "Hits" should "be decoded" in {

  }
}
