package meetup.akka.fsm

import akka.actor._
import meetup.akka.Api
import meetup.akka.fsm.Receptionist.Internal.{NoQueue, Processing, Queue, Sleeping}
import meetup.akka.fsm.Receptionist._

object Receptionist {

  case class Result(links: Set[String])

  protected case class Job(client: ActorRef, url: String, depth: Int)

  object Internal {

    sealed trait State
    case object Sleeping extends State
    case object Processing extends State

    sealed trait Data
    case class NoQueue(requestId: Int = 0) extends Data
    case class Queue(currentRequestId: Int, items: Vector[Job]) extends Data

  }

}

class Receptionist extends LoggingFSM[Internal.State, Internal.Data] with LogEntriesToStringSupport[Internal.State, Internal.Data] {

  startWith(Sleeping, NoQueue())

  when(Sleeping)(enqueueNewRequest)

  when(Processing) (processResult orElse enqueueNewRequest orElse reportError)

  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  override def logDepth = 8

  def enqueueNewRequest: StateFunction = {
    case Event(Api.Scrape(url, depth), NoQueue(requestId)) =>
      startControllerFor(requestId + 1, Vector(Job(sender(), url, depth)))
    case Event(Api.Scrape(url, depth), queue: Queue) =>
      if (queue.items.size > 3) {
        stay replying Api.Failed(url)
      } else {
        goto(Processing) using Queue(queue.currentRequestId, queue.items :+ Job(sender(), url, depth))
      }
  }

  def processResult: StateFunction = {
    case Event(Result(links), queue: Queue) =>
      val job = queue.items.head
      job.client ! Api.Result(job.url, links)
      context.stop(context.unwatch(sender()))
      nextQueueItem(queue)
  }

  def reportError: StateFunction = {
    case Event(Terminated(_), queue: Queue) =>
      queue.items.head.client ! Api.Failed(queue.items.head.url)
      nextQueueItem(queue)
  }

  private def nextQueueItem(queue: Queue): State = {
    val remainingItems = queue.items.tail
    if (remainingItems.isEmpty) {
      goto(Sleeping) using NoQueue(queue.currentRequestId)
    } else {
      startControllerFor(queue.currentRequestId + 1, remainingItems)
    }
  }

  private def startControllerFor(requestId: Int, queue: Vector[Job]): State = {
    val controller = context.actorOf(Props[Controller], s"controller-$requestId")
    context.watch(controller)
    controller ! Controller.Check(queue.head.url, queue.head.depth)
    goto(Processing) using Queue(requestId, queue)
  }

  whenUnhandled {
    case Event(any, data) =>
      val logUpToHere = prettyPrint(getLog)
      log.error(s"Unhandled event: ${any}\n$logUpToHere")
      stay()
  }

  initialize()
}
