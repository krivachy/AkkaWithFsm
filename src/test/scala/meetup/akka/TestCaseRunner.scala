package meetup.akka

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.Config
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

abstract class TestCaseRunner(name: String, receptionistProps: Props, config: Config) extends TestKit(ActorSystem("simple-actors", config)) with ImplicitSender with FlatSpecLike with Matchers with TestConfig with BeforeAndAfterAll {

  val receptionist = system.actorOf(receptionistProps, "receptionist")

  s"$name scraper" should "correctly scrape What is Akka" in {
    runTestCase(TestCases.`Akka2.3.5-WhatIsAkka`)
  }

  it should "correctly scrape Zombo.com" in {
    runTestCase(TestCases.`Zombo.com`)
  }

  it should "return if the link is wrong" in {
    runTestCase(TestCases.BadLink)
  }

  it should "reject if queue is full" in {
    receptionist ! Api.Scrape("http://non.existent1", 0)
    receptionist ! Api.Scrape("http://non.existent2", 0)
    receptionist ! Api.Scrape("http://non.existent3", 0)
    receptionist ! Api.Scrape("http://non.existent4", 0)
    receptionist ! Api.Scrape("http://non.existent5", 0)
    expectMsg(Api.Failed("http://non.existent5"))
  }


  def runTestCase(testCase: TestCases.TestCase): Unit = {
    receptionist ! Api.Scrape(testCase.url, testCase.depth)
    val scaledTimeout = OneLinkTimeout * BigInt(2).pow(testCase.depth).toInt
    expectMsg(scaledTimeout, Api.Result(testCase.url, testCase.result))
  }

  override protected def afterAll(): Unit = {
    system.stop(receptionist)
    super.afterAll()
  }
}
