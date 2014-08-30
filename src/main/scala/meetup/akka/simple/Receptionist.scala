package meetup.akka.simple

import akka.actor._
import akka.event.LoggingReceive
import meetup.akka.Api

object Receptionist {
  case class Result(links: Set[String])
  protected case class Job(client: ActorRef, url: String, depth: Int)
}

class Receptionist extends Actor {
  import meetup.akka.simple.Receptionist._

  var reqId = 0

  def receive = waiting

  def waiting: Receive = {
    case Api.Scrape(url, depth) => context.become(next(Vector(Job(sender, url, depth))))
  }

  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  def running(queue: Vector[Job]): Receive = LoggingReceive {
    case Result(links) =>
      val job = queue.head
      job.client ! Api.Result(job.url, links)
      context.stop(context.unwatch(sender))
      context.become(next(queue.tail))
    case Api.Scrape(url, depth) =>
      context.become(enqueue(queue, Job(sender, url, depth)))
    case Terminated(_) =>
      val job = queue.head
      job.client ! Api.Failed(job.url)
      context.become(next(queue.tail))
  }

  def enqueue(queue: Vector[Job], job: Job): Receive = LoggingReceive {
    if (queue.size > 3) {
      sender ! Api.Failed(job.url)
      running(queue)
    } else running(queue :+ job)
  }

  def next(queue: Vector[Job]): Receive = LoggingReceive {
    reqId += 1
    if (queue.isEmpty) waiting
    else {
      val controller = context.actorOf(Props[Controller], s"controller-$reqId")
      context.watch(controller)
      controller ! Controller.Check(queue.head.url, queue.head.depth)
      running(queue)
    }
  }
}
