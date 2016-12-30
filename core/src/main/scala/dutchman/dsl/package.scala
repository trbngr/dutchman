package dutchman

package object dsl {
  import api._
  import cats.free.Free
  import cats.free.Free.liftF

  implicit val defaultDocument = new ESDocument[Document] {
    def document(a: Document) = a
  }

  type ElasticsearchApi[A] = Free[Api, A]

  def index[A: ESDocument](index: Idx, `type`: Type, document: A, version: Option[Int] = None): ElasticsearchApi[IndexResponse] = liftF(Index(index, `type`, document, version))
  def get[Json](index: Idx, `type`: Type, id: Id): ElasticsearchApi[GetResponse[Json]] = liftF(Get(index, `type`, id))

}