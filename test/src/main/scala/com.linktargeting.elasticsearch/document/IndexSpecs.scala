package com.linktargeting.elasticsearch.document

import com.linktargeting.elasticsearch.ApiSpecs
import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.model._

trait IndexSpecs[Json] {
  this: ApiSpecs[Json] ⇒

  private[this] val idx: Idx = "index_specs"

  "Index" when {
    val person = Person("123", "Chris", "PHX")

    "it doesn't exist" should {
      "be created" in {
        val api = Index(idx, tpe, person)
        val response = dsl.document(api).futureValue
        response.created shouldBe true
        deleteIndex(idx)
      }
    }

    "It already exists" should {
      "be not created" in {
        val api = Index(idx, tpe, person)
        whenReady(dsl.document(api)) { _ ⇒
          val response = dsl.document(api).futureValue
          response.created shouldBe false
          deleteIndex(idx)
        }
      }
    }
  }
}
