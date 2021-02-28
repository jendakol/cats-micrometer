package cz.jenda.cats.micrometer

import cats.effect.Sync
import io.micrometer.core.instrument.{Counter => Delegate}

class DefaultCounter[F[_]: Sync](override protected val delegate: Delegate) extends DefaultMeter with Counter[F] {

  private val F = Sync[F]

  override def increment: F[Unit] = increment(1.0)
  override def increment(amount: Double): F[Unit] = F.delay(delegate.increment(amount))
  override def count: F[Double] = F.delay(delegate.count())

}
