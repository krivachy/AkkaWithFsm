package meetup.akka

import scala.concurrent.duration._

trait TestConfig {
  val OneLinkTimeout = 5.seconds

  object TestCases {

    case class TestCase(url: String, depth: Int, result: Set[String])

    val `Akka2.3.5-WhatIsAkka` = TestCase("http://doc.akka.io/docs/akka/2.3.5/intro/what-is-akka.html", 1, Set("http://github.com/akka/akka/tree/v2.3.5/akka-docs/rst/java/code/docs", "http://akka.io", "http://akka.io/downloads", "http://doc.akka.io/docs/akka/current/project/issue-tracking.html", "http://github.com/akka/akka", "http://akka.io/team", "http://akka.io/community", "http://www.typesafe.com/how/subscription", "http://akka.io/news", "http://akka.io/faq", "http://github.com/akka/akka/tree/v2.3.5/akka-docs/rst/scala/code/docs", "http://akka.io/docs", "http://letitcrash.com", "http://groups.google.com/group/akka-user", "http://doc.akka.io/docs/akka/2.3.5/intro/what-is-akka.html", "http://www.typesafe.com/"))
    val `Zombo.com` = TestCase("http://zombo.com/", 3, Set("http://zombo.com/"))
    val BadLink = TestCase("http://non-existent.link", 5, Set("http://non-existent.link"))
  }


}
