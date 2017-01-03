package dutchman.dsl

trait Syntax {

  implicit def stringToId(s: String): Id = Id(s)
  implicit def stringToIdx(s: String): Idx = Idx(s)
  implicit def stringToType(s: String): Type = Type(s)
  implicit def stringsToIndices(s: Seq[String]): Seq[Idx] = s.map(stringToIdx)
  implicit def stringsToTypes(s: Seq[String]): Seq[Type] = s.map(stringToType)
}
