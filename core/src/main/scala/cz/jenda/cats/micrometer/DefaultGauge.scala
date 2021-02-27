package cz.jenda.cats.micrometer

import cats.effect.Sync
import io.micrometer.core.instrument.{Gauge => Delegate}

class DefaultGauge[F[_]: Sync](delegate: Delegate) extends Gauge[F] {

  private val F = Sync[F]

  override def value: F[Double] = F.delay(delegate.value)

}
