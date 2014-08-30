package meetup.akka.simple

import akka.actor.Props
import meetup.akka.{AkkaConfig, TestCaseRunner}

class SimpleActorTest extends TestCaseRunner("simple-actor", Props[Receptionist], AkkaConfig.FullLogConfig) {

}
