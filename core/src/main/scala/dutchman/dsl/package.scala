package dutchman

package object dsl extends OpDsl with QueryDsl with SearchOptionsDsl with Syntax {

  case class ElasticDocument(id: Id, data: Map[String, Any])

  trait ESDocument[A] {
    def document(a: A): ElasticDocument
  }

  case class Shards(total: Int, failed: Int, successful: Int)
  case class Response(shards: Shards, index: String, `type`: String, id: String, version: Int)
  case class ESError(`type`: String, reason: String, resourceType: String, resourceId: Id, index: Idx, status: Int) extends Exception(s"Elasticsearch Exception: $reason")

  final case class Id(value: String)

  object Idx {
    def apply(name: String): Idx = new Idx(name.toLowerCase())
  }

  final class Idx(val name: String) {
    override def equals(obj: scala.Any): Boolean = obj match {
      case other: Idx ⇒ other.name.equals(name)
      case _          ⇒ false
    }
  }

  final case class Type(name: String)

}
