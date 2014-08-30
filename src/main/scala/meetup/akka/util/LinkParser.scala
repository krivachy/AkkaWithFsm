package meetup.akka.util

trait LinkParser {
  val anchors = "(?i)<a ([^>]+)>.+?</a>".r
  val href = """.*\s*(?i)href\s*=\s*(?:"(http[^"#]*)"|'(http[^']*)'|(http[^'">\s]+)).*""".r

  def parseLinks(body: String): Iterator[String] =
    for {
      a <- anchors.findAllMatchIn(body)
      href(dquot, quot, noquot) <- a.subgroups
    } yield
      if (dquot != null) dquot
      else if (quot != null) quot
      else noquot
}
