package cz.jenda.cats.micrometer

import cats.effect.{Bracket, Sync}
import io.micrometer.core.instrument.{Timer => Delegate}
import cats.syntax.all._
import java.time.{Duration => JavaDuration}
import java.util.concurrent.{Callable, TimeUnit}
import scala.concurrent.duration.Duration

class DefaultTimer[F[_]: Sync](override protected val delegate: Delegate, clock: F[Long]) extends DefaultMeter with Timer[F] {

  private val F = Sync[F]

  override def record(duration: JavaDuration): F[Unit] = F.delay(delegate.record(duration))

  override def record(duration: Duration): F[Unit] = {
    F.delay(delegate.record(duration.toNanos, TimeUnit.NANOSECONDS))
  }

  override def wrap[A](block: => A): F[A] = {
    F.delay {
      delegate
        .wrap(new Callable[A] {
          override def call(): A = block
        })
        .call()
    }
  }

  override def wrap[A](f: F[A]): F[A] = {
    // TODO replace timing with clocks from registry

    Bracket[F, Throwable].bracket(clock)(_ => f)(start => clock.map(end => delegate.record(end - start, TimeUnit.NANOSECONDS)))
  }

  override def count: F[Double] = F.delay(delegate.count().toDouble)

  override def totalTime(unit: TimeUnit): F[Double] = F.delay(delegate.totalTime(unit))

}
