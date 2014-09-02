package meetup.akka.typed

import com.ning.http.client.AsyncHttpClientConfig.Builder
import meetup.akka.util.LinkParser
import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.{ExecutionContext, Future}

object Getter extends LinkParser {
  case class Urls(urls: Set[String], depth: Int)

  lazy val client = new NingWSClient(new Builder().build())

  def get(url: String, depth: Int)(implicit ec: ExecutionContext): Future[Urls] = {
    client.url(url).get().map {
      response =>
        Urls(parseLinks(response.body).toSet, depth)
    }
  }
}