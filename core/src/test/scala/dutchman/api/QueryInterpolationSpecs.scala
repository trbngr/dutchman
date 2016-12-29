package dutchman.api

import org.scalatest.{Matchers, WordSpec}

class QueryInterpolationSpecs extends WordSpec with Matchers {
  "prefix" should {
    "create a query without boost" in {
      val query = prefix"name:chris"
      query shouldBe Prefix("name", "chris")
    }
    "create a query with boost" in {
      val boost = 2
      val name = "chris"
      val query = prefix"name:$name:$boost"
      query shouldBe Prefix("name", "chris", 2)
    }
  }

  "wildcard" should {
    "create a query without boost" in {
      val query = wildcard"name:chris"
      query shouldBe Wildcard("name", "chris")
    }
    "create a query with boost" in {
      val boost = 2
      val name = "chris"
      val query = wildcard"name:$name:$boost"
      query shouldBe Wildcard("name", "chris", 2)
    }
  }

  "term" should {
    "create a query without boost" in {
      val query = term"name:chris"
      query shouldBe Term("name", "chris")
    }
    "create a query with boost" in {
      val boost = 2
      val name = "chris"
      val query = term"name:$name:$boost"
      query shouldBe Term("name", "chris", 2)
    }
  }
}
