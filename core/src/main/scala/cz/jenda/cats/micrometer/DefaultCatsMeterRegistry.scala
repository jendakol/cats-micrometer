package cz.jenda.cats.micrometer

import cats.effect.{Resource, Sync}
import cz.jenda.cats.micrometer.DefaultCatsMeterRegistry.CollectionSizeToDouble
import cz.jenda.cats.micrometer.MicrometerJavaConverters._
import io.micrometer.core.instrument.{Gauge => JavaGauge, MeterRegistry => JavaMeterRegistry, Counter => _, Tag => _, Timer => _}

class DefaultCatsMeterRegistry[F[_]: Sync] private (delegate: JavaMeterRegistry) extends CatsMeterRegistry[F] {

  private val F = Sync[F]

  private val clock: F[Long] = F.delay(delegate.config().clock().monotonicTime())

  override def underlying: JavaMeterRegistry = delegate

  override def counter(name: String, tags: Iterable[Tag]): Resource[F, Counter[F]] = {
    F.delayedResource((delegate.counter(name, tags.asJavaTags))).map(new DefaultCounter(_))
  }

  override def counter(name: String, tags: Tag*): Resource[F, Counter[F]] = counter(name, tags)

  override def timer(name: String, tags: Iterable[Tag]): Resource[F, Timer[F]] = {
    F.delayedResource(delegate.timer(name, tags.asJavaTags)).map(new DefaultTimer(_, clock))
  }

  override def timer(name: String, tags: Tag*): Resource[F, Timer[F]] = timer(name, tags)

  override def gauge[A: ToDouble](name: String, tags: Iterable[Tag])(retrieveValue: () => A): Resource[F, Gauge[F]] = {
    val conv = implicitly[ToDouble[A]]

    F.delayedResource {
        JavaGauge.builder(name, retrieveValue, (value: () => A) => conv.toDouble(value())).tags(tags.asJavaTags).register(delegate)
      }
      .map(new DefaultGauge[F](_))
  }

  override def gauge[A: ToDouble](name: String)(retrieveValue: () => A): Resource[F, Gauge[F]] = gauge(name, List.empty)(retrieveValue)

  override def gaugeCollectionSize[A <: Iterable[_]](name: String, tags: Iterable[Tag], collection: A): Resource[F, Gauge[F]] = {
    gauge(name, tags)(() => collection)(CollectionSizeToDouble)
  }

  override def timerPair(name: String, tags: Iterable[Tag]): Resource[F, TimerPair[F]] = ???

  override def timerPair(name: String, tags: Tag*): Resource[F, TimerPair[F]] = ???

  override def clear: F[Unit] = F.delay(delegate.clear())

  override def meters: F[Seq[Meter]] = F.delay(delegate.getMeters.asScalaMeters)

  override def remove(meter: Meter): F[Option[Meter]] = remove(meter.id)

  override def remove(meterId: MeterId): F[Option[Meter]] = {
    F.delay(Option(delegate.remove(meterId.asJava).asScala))
  }
}

object DefaultCatsMeterRegistry {

  def wrap[F[_]: Sync](delegate: => JavaMeterRegistry): Resource[F, CatsMeterRegistry[F]] = {
    val F = Sync[F]

    Resource.make(F.delay(delegate))(r => F.delay(r.close())).map { delegate =>
      new DefaultCatsMeterRegistry[F](delegate)
    }
  }

  private object CollectionSizeToDouble extends ToDouble[Iterable[_]] {
    override def toDouble(value: Iterable[_]): Double = value.size.toDouble
  }

}
