package com.linktargeting.elasticsearch

import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.api.translation.ESDocument

object model {
  case class Person(id: String, name: String, city: String)

  implicit val personDocument = new ESDocument[Person] {
    def document(a: Person): Document = Document(Id(a.id), Map("id" → a.id, "name" → a.name, "city" → a.city))
  }
}
