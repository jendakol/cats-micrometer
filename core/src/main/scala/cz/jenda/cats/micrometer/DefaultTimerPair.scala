package cz.jenda.cats.micrometer

import cats.effect.{Bracket, ExitCase, Sync}
import cats.syntax.all._

import java.time.{Duration => JavaDuration}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

class DefaultTimerPair[F[_]: Sync](val successes: Timer[F], val failures: Timer[F], clock: F[Long]) extends TimerPair[F] {

  private val F = Sync[F]

  override def recordSuccess(duration: JavaDuration): F[Unit] = F.delay(successes.record(duration))

  override def recordSuccess(duration: Duration): F[Unit] = {
    F.delay(successes.record(duration.toNanos, TimeUnit.NANOSECONDS))
  }

  override def recordFailure(duration: JavaDuration): F[Unit] = F.delay(failures.record(duration))

  override def recordFailure(duration: Duration): F[Unit] = {
    F.delay(failures.record(duration.toNanos, TimeUnit.NANOSECONDS))
  }

  override def wrap[A](block: => A): F[A] = {
    clock.flatMap { start =>
      try {
        val result = block
        clock.map { end =>
          successes.record(end - start, TimeUnit.NANOSECONDS)
          result
        }
      } catch {
        case NonFatal(e) =>
          clock.flatMap { end =>
            failures.record(end - start, TimeUnit.NANOSECONDS)
            F.raiseError(e)
          }
      }
    }
  }

  override def wrap[A](f: F[A]): F[A] = {
    Bracket[F, Throwable].bracketCase(clock)(_ => f) {
      case (start, ExitCase.Completed) => clock.map(end => successes.record(end - start, TimeUnit.NANOSECONDS))
      case (start, ExitCase.Error(_))  => clock.map(end => failures.record(end - start, TimeUnit.NANOSECONDS))
      case _                           => F.unit
    }
  }

  override def count: F[Double] = {
    for {
      s <- countSuccesses
      f <- countFailures
    } yield {
      s + f
    }
  }

  override def countSuccesses: F[Double] = successes.count
  override def countFailures: F[Double] = failures.count

  override def totalTime(unit: TimeUnit): F[Double] = {
    for {
      s <- totalTimeSuccesses(unit)
      f <- totalTimeFailures(unit)
    } yield {
      s + f
    }
  }

  override def totalTimeSuccesses(unit: TimeUnit): F[Double] = successes.totalTime(unit)

  override def totalTimeFailures(unit: TimeUnit): F[Double] = failures.totalTime(unit)

}
