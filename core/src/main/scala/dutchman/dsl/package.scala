package dutchman

package object dsl extends OpDsl with QueryDsl with SearchOptionsDsl with Syntax {

  case class ElasticDocument(id: Id, data: Map[String, Any])

  trait ESDocument[A] {
    def document(a: A): ElasticDocument
  }

  case class Shards(total: Int, failed: Int, successful: Int)
  case class Response(shards: Shards, index: String, `type`: String, id: String, version: Int)
  case class ESError(index: String, `type`: String, id: String, status: Int)
  case class ElasticErrorsException(errors: Seq[ESError]) extends Exception(s"Elasticsearch exception: ${errors.map(e ⇒ e.status).mkString("\n")}")

  final case class Id(value: String)

  object Idx {
    def apply(name: String): Idx = new Idx(name.toLowerCase())
  }
  final class Idx(val name: String) {
    override def toString = name
  }

  final case class Type(name: String)

}
