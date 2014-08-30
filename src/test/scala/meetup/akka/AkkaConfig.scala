package meetup.akka

import com.typesafe.config.ConfigFactory

object AkkaConfig {
  val FullLogConfig = ConfigFactory.parseString("""
    |akka.loglevel=DEBUG
    |akka.actor.debug.lifecycle=true
    |akka.actor.debug.receive=true
    |akka.actor.debug.autoreceive=true
  """.stripMargin)
}
