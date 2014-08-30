package meetup.akka.simple

import akka.actor.{Actor, ActorLogging, Status}
import akka.pattern.pipe
import com.ning.http.client.AsyncHttpClientConfig.Builder
import meetup.akka.util.LinkParser
import play.api.libs.ws.ning.NingWSClient


object Getter {
  case class Response(body: String)
}

class Getter(url: String, depth: Int) extends Actor with ActorLogging with LinkParser {
  import context.dispatcher
  import meetup.akka.simple.Getter._

  lazy val client = new NingWSClient(new Builder().build()).url(url)

  override def preStart() = {
    client.get().map(_.body).map(Response).pipeTo(self)
  }

  override def postStop() = {}

  def receive = {
    case Response(body) =>
      val links = parseLinks(body).toList
      log.info(s"URL $url at depth $depth had ${links.size} links.")
      links.foreach {
        link =>
          log.info(s"Sending link '$link'")
          context.parent ! Controller.Check(link, depth)
      }
      context.stop(self)
    case Status.Failure(cause) =>
      log.error(s"Failed to GET $url", cause)
      context.stop(self)
  }

}
