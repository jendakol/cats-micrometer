package cz.jenda.cats.micrometer

import cats.effect.{Resource, Sync}
import cz.jenda.cats.micrometer.DefaultCatsEffectMeterRegistry.CollectionSizeToDouble
import io.micrometer.core.instrument.{MeterRegistry => JavaMeterRegistry, Counter => _, Gauge => _, Tag => _, Timer => _}

class DefaultCatsEffectMeterRegistry[F[_]: Sync] private (delegate: JavaMeterRegistry) extends MeterRegistry[F] {

  private val F = Sync[F]

  override def counter(name: String, tags: Iterable[Tag]): F[Counter[F]] = {
    F.delay(new DefaultCounter(delegate.counter(name, tags.asJavaTags)))
  }

  override def counter(name: String, tags: Tag*): F[Counter[F]] = counter(name, tags)

  override def timer(name: String, tags: Iterable[Tag]): F[Timer[F]] = {
    F.delay(new DefaultTimer(delegate.timer(name, tags.asJavaTags)))
  }

  override def timer(name: String, tags: Tag*): F[Timer[F]] = timer(name, tags)

  override def gauge[A: ToDouble](name: String, tags: Iterable[Tag], numberLike: A): F[Gauge[F]] = {
    F.delay {
      delegate.gauge[Double](name, tags.asJavaTags, ToDouble[A].toDouble(numberLike), (_: Double).doubleValue())
      new DefaultGauge(delegate.get(name).gauge())
    }
  }

  override def gauge[A: ToDouble](name: String, numberLike: A): F[Gauge[F]] = gauge(name, List.empty, numberLike)

  override def gaugeCollectionSize[A <: Iterable[_]](name: String, tags: Iterable[Tag], collection: A): F[Gauge[F]] = {
    gauge(name, tags, collection)(CollectionSizeToDouble)
  }

  override def clear: F[Unit] = F.delay(delegate.clear())
}

object DefaultCatsEffectMeterRegistry {

  def wrap[F[_]: Sync](delegate: => JavaMeterRegistry): Resource[F, MeterRegistry[F]] = {
    val F = Sync[F]

    Resource.make(F.delay(delegate))(r => F.delay(r.close())).map { delegate =>
      new DefaultCatsEffectMeterRegistry[F](delegate)
    }
  }

  private object CollectionSizeToDouble extends ToDouble[Iterable[_]] {
    override def toDouble(value: Iterable[_]): Double = value.size.toDouble
  }

}
