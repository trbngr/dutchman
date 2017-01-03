package dutchman

import dutchman.dsl._

object model {
  case class Person(id: String, name: String, city: String)

  implicit val personDocument = new ESDocument[Person] {
    def document(a: Person): ElasticDocument = ElasticDocument(a.id, Map("id" → a.id, "name" → a.name, "city" → a.city))
  }
}
