package utils

import scala.reflect.ClassTag

trait WithAsString {
  def asString: String
}

trait StringCompanion[T <: WithAsString] {
  def all: Set[T]
  def fromStringOpt(str: String): Option[T] = all.collectFirst {
    case ele if ele.asString == str => ele
  }

  def fromString(str: String)(implicit classTag: ClassTag[T]): T = fromStringOpt(str).getOrElse(
    throw new Exception(s"String $str is not a defined type of ${classTag.runtimeClass.getName}"))
}
