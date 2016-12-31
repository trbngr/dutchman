package dutchman

import dutchman.api._
import marshalling._

object testMarshalling extends testMarshalling

trait testMarshalling {
  implicit object writer extends OperationWriter {
    override def write[A](api: Api[A]) = ""
  }

  implicit object reader extends ResponseReader[String]{
    override def read(json: String) = json
    override def error(json: String) = ESError("" , "", "", 500)
    override def index(json: String) = ???
    override def deleteIndex(json: String) = ???
    override def readError(json: String) = None
    override def bulk(json: String) = ???
    override def search(json: String) = ???
    override def refresh(json: String) = ???
    override def scroll(json: String) = ???
    override def get(json: String) = ???
    override def multiGet(json: String) = ???
    override def delete(json: String) = ???
    override def update(json: String) = ???
  }
}
