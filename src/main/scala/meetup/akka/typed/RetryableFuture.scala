package meetup.akka.typed

import scala.concurrent.{ExecutionContext, Future}

object RetryableFuture {
  def apply[T](retryMaxNumberOfTimes: Int)(retryStrategy: PartialFunction[Throwable, Boolean])(body: => T)(implicit ec: ExecutionContext): Future[T] = {
    Future(body).recoverWith {
      case exception if retryStrategy.lift(exception) == Some(true) && retryMaxNumberOfTimes > 0 =>
        RetryableFuture(retryMaxNumberOfTimes - 1)(retryStrategy)(body)
    }
  }
}
