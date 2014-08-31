package meetup.akka.fsm

import akka.actor._
import meetup.akka.fsm.Controller.Internal._
import meetup.akka.fsm.Controller._

object Controller {

  case class Check(link: String, depth: Int)

  object Internal {
    sealed trait State
    case object CollectingResults extends State
    case object Completed extends State

    sealed trait Data
    case class ResultHolder(results: Set[String]) extends Data
    case object NoData extends Data
  }

}

class Controller extends LoggingFSM[Controller.Internal.State, Controller.Internal.Data] with LogEntriesToStringSupport[Controller.Internal.State, Controller.Internal.Data] {

  startWith (CollectingResults, ResultHolder(Set.empty))

  when (CollectingResults) (startGettersForNewLinks orElse respondAfterAllChildrenComplete)

  when (Completed) (noOperation)

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 5) {
    case _: Exception => SupervisorStrategy.Restart
  }

  override def logDepth = 8

  def startGettersForNewLinks: StateFunction = {
    case Event(Check(url, depth), ResultHolder(results)) =>
      log.info("{} checking {}", depth, url)
      if (!results(url) && depth > 0)
        context.watch(context.actorOf(Props(new Getter(url, depth - 1))))
      stay using ResultHolder(results + url)
  }

  def respondAfterAllChildrenComplete: StateFunction = {
    case Event(Terminated(_), ResultHolder(results)) =>
      if (context.children.isEmpty) {
        context.parent ! Receptionist.Result(results)
        goto(Completed) using NoData
      } else stay()
  }

  def noOperation: StateFunction = {
    case Event(_, _) =>
      log.error(s"Received message when already complete.\n${prettyPrint(getLog)}")
      stay()
  }

  initialize()
}