package com.linktargeting.elasticsearch.actor

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import com.linktargeting.elasticsearch.api._
import com.linktargeting.elasticsearch.dsl._
import com.linktargeting.elasticsearch.marshalling._

import scala.collection.immutable.Queue

object BulkIndexer {
  case object Flush
  case class IndexingSuccessful(data: Seq[(BulkAction, SingleDocumentApi, BulkResponse, ActorRef)])
  case class IndexingFailure(error: String, data: Seq[(BulkAction, SingleDocumentApi, ActorRef)])
  case class DocumentIndexed(response: BulkResponse)
  case class DocumentNotIndexed(cause: String, action: BulkAction, api: SingleDocumentApi)

  def props[Json](client: Dsl[Json], config: BulkIndexerConfig)(implicit marshaller: ApiMarshaller): Props = {
    Props(new BulkIndexer[Json](client, config))
  }

  def props[Json](client: Dsl[Json])(implicit marshaller: ApiMarshaller): Props = props(client, BulkIndexerConfig())
}

class BulkIndexer[Json](client: Dsl[Json], config: BulkIndexerConfig)(implicit val marshaller: ApiMarshaller) extends Actor {

  import BulkIndexer._
  import akka.pattern.pipe

  implicit val ec = context.system.dispatcher

  val log = Logging(this)
  val flushSchedule = context.system.scheduler.schedule(config.flushDuration, config.flushDuration, self, Flush)
  var queue = Queue.empty[(BulkAction, SingleDocumentApi, ActorRef)]

  override def receive = {
    case (a: BulkAction, api: SingleDocumentApi) ⇒ queue = queue.enqueue((a, api, sender())); flush()
    case Flush                                   ⇒ flush(force = true)
    case IndexingSuccessful(data)                ⇒
      data foreach {
        case (_, _, response, replyTo) ⇒ replyTo ! DocumentIndexed(response)
      }
    case IndexingFailure(error, data)            ⇒
      data foreach {
        case (action, api, sender) ⇒ sender ! DocumentNotIndexed(error, action, api)
      }
    case other                                   ⇒ log.warning(s"Uknown message: ${other.getClass} -> $other")
  }

  override def postStop() = {
    flushSchedule.cancel()
    flush(force = true)
    super.postStop()
  }

  def flush(force: Boolean = false) = {
    if ((force || queue.size == config.maxDocuments) && queue.nonEmpty) {

      val api = Bulk(queue.map {
        case (action, docApi, _) ⇒ action → docApi
      }: _*)

      val bulkSession = queue
      queue = Queue.empty[(BulkAction, SingleDocumentApi, ActorRef)]

      val json = translation.apiRequest(api).payload
      val payloadSizeInMb = json.length / 1024.0 / 1024

      log.info(f"Flushing ${api.actions.size} messages ($payloadSizeInMb%.2fMB).")

      client.document(api) map { response ⇒
        log.info(f"Flushed ${response.size} messages ($payloadSizeInMb%.2fMB).")
        if (response.size == bulkSession.size) {
          IndexingSuccessful(response.zip(bulkSession) map {
            case (bulkResponse, (action, docApi, replyTo)) => (action, docApi, bulkResponse, replyTo)
          })
        } else {
          log.error(s"Number of responses ${response.size} != requests(${bulkSession.length}).")
          IndexingFailure("Response count mismatch.", bulkSession)
        }
      } recover {
        case e: Throwable =>
          log.error(s"Failed to flush ${bulkSession.size} messages.", e)
          IndexingFailure(e.getMessage, bulkSession)
      } pipeTo self
    }
  }
}
