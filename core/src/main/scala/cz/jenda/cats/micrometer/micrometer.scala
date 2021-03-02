package cz.jenda.cats

import cats.effect.{Resource, Sync}
import io.micrometer.core.instrument.{Meter => JavaMeter}

package object micrometer {
  private[micrometer] implicit class MeterWrap[F[_]](val F: Sync[F]) extends AnyVal {
    def delayedResource[A <: JavaMeter](a: => A): Resource[F, A] = {
      Resource.make(F.delay(a))(a => F.delay(a.close()))(F)
    }
  }
}
