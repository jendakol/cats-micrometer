package cz.jenda.cats.micrometer

import cats.effect.Sync
import io.micrometer.core.instrument.{Gauge => Delegate}

class Gauge[F[_]: Sync](delegate: Delegate) {

  private val F = Sync[F]

  def value: F[Double] = F.delay(delegate.value)

}
