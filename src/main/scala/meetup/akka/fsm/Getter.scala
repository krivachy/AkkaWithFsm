package meetup.akka.fsm

import akka.actor._
import akka.pattern.pipe
import com.ning.http.client.AsyncHttpClientConfig.Builder
import meetup.akka.fsm.Getter.Internal.{NoData, WaitingForGetResult}
import meetup.akka.fsm.Getter.Response
import meetup.akka.util.LinkParser
import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.duration._

object Getter {
  case class Response(body: String)

  object Internal {
    sealed trait State
    case object WaitingForGetResult extends State

    sealed trait Data
    case object NoData extends Data
  }
}

// FSM quite pointless here
class Getter(url: String, depth: Int) extends LoggingFSM[Getter.Internal.State, Getter.Internal.Data] with LinkParser {
  import context.dispatcher

  lazy val client = new NingWSClient(new Builder().build()).url(url)

  startWith(WaitingForGetResult, NoData)

  when(WaitingForGetResult, 3.seconds) (processResult orElse failure)

  override def preStart() = {
    client.get().map(_.body).map(Response).pipeTo(self)
  }

  def processResult: StateFunction = {
    case Event(Response(body), _) =>
      val links = parseLinks(body).toList
      log.info(s"URL $url at depth $depth had ${links.size} links.")
      links.foreach {
        link =>
          log.info(s"Sending link '$link'")
          context.parent ! Controller.Check(link, depth)
      }
      stop()
  }
  def failure: StateFunction = {
    case Event(Status.Failure(cause), _) =>
      log.error(s"Failed to GET $url", cause)
      stop(FSM.Failure(cause))
  }

  initialize()
}
