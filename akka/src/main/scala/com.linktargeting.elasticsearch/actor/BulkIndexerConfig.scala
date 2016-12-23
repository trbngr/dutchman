package com.linktargeting.elasticsearch.actor

import scala.concurrent.duration._

object BulkIndexerConfig {
  def apply(): BulkIndexerConfig = BulkIndexerConfig(15 seconds, 25)
}

case class BulkIndexerConfig(flushDuration: FiniteDuration, maxDocuments: Int)