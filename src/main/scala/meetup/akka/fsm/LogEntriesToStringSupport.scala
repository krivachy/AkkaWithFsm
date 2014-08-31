package meetup.akka.fsm

import akka.actor.FSM
import akka.actor.FSM.LogEntry

trait LogEntriesToStringSupport[S, D] {
  self: FSM[S, D] =>

  def prettyPrint(logEntries: Seq[LogEntry[S, D]]): String = {
    val entriesInString = logEntries.map{
      e =>
        s"""  in state: ${e.stateName}
           |   with data: ${e.stateData}
           |   received: ${e.event.toString}
         """.stripMargin
    }.mkString("\n")

    s"Last ${logEntries.size} entries leading up to this point:\n$entriesInString"
  }

}
