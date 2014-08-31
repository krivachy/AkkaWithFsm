package meetup.akka.fsm

import akka.actor.Props
import meetup.akka.{AkkaConfig, TestCaseRunner, fsm}

import scala.concurrent.duration._

class FsmTest extends TestCaseRunner("fsm", Props[fsm.Receptionist], AkkaConfig.FSMConfig) {

  it should "print the log entries nicely" in {
    receptionist ! "some string"
    // Check logs
    expectNoMsg(2.seconds)
  }

}
