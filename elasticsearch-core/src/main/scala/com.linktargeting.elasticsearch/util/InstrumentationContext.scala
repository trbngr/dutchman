package com.linktargeting.elasticsearch.util

import scala.collection.immutable.Queue

class InstrumentationContext {

  var results = Queue.empty[(String, Long)]

  def measure[T](name: String)(f: ⇒ T): T = {
    val start = System.currentTimeMillis
    val result = f
    results = results.enqueue(name → (System.currentTimeMillis - start))
    result
  }

  override def toString = results map {
    case (actionName, measurement) => s"$actionName='${measurement}ms'"
  } mkString ", "
}
