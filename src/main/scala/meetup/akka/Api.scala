package meetup.akka

/**
 * Common API for all sample implementations
 */
object Api {
  case class Scrape(url: String, depth: Int)
  case class Result(url: String, links: Set[String])
  case class Failed(url: String)
}