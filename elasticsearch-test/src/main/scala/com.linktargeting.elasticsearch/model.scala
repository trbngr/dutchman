package com.linktargeting.elasticsearch

import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.api.translation.EsDocument

object model {
  case class Person(id: String, name: String, city: String)

  implicit object PersonMapper extends EsDocument[Person] {
    override def asDocument(a: Person): Document = Document(Id(a.id), Map("id" → a.id, "name" → a.name, "city" → a.city))
  }
}
