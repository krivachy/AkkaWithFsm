package meetup.akka.simple

import akka.actor._
import akka.event.LoggingReceive

object Controller {
  case class Check(link: String, depth: Int)
}

class Controller extends Actor with ActorLogging {
  import meetup.akka.simple.Controller._

  var cache = Set.empty[String]

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 5) {
    case _: Exception => SupervisorStrategy.Restart
  }

  def receive = LoggingReceive {
    case Check(url, depth) =>
      log.info("{} checking {}", depth, url)
      if (!cache(url) && depth > 0)
        context.watch(context.actorOf(Props(new Getter(url, depth - 1))))
      cache += url
    case Terminated(_) =>
      if (context.children.isEmpty) context.parent ! Receptionist.Result(cache)
  }
}