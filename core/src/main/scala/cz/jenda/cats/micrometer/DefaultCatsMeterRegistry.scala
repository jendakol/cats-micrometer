package cz.jenda.cats.micrometer

import cats.effect.{Resource, Sync}
import cz.jenda.cats.micrometer.DefaultCatsMeterRegistry.CollectionSizeToDouble
import cz.jenda.cats.micrometer.MicrometerJavaConverters._
import io.micrometer.core.instrument.{Gauge => JavaGauge, MeterRegistry => JavaMeterRegistry, Counter => _, Tag => _, Timer => _}

class DefaultCatsMeterRegistry[F[_]: Sync] private (delegate: JavaMeterRegistry) extends CatsMeterRegistry[F] {

  private val F = Sync[F]

  private val clock: F[Long] = F.delay(delegate.config().clock().monotonicTime())

  override def underlying: JavaMeterRegistry = delegate

  override def counter(name: String, tags: Iterable[Tag]): F[Counter[F]] = {
    F.delay(new DefaultCounter(delegate.counter(name, tags.asJavaTags)))
  }

  override def counter(name: String, tags: Tag*): F[Counter[F]] = counter(name, tags)

  override def timer(name: String, tags: Iterable[Tag]): F[Timer[F]] = {
    F.delay(new DefaultTimer(delegate.timer(name, tags.asJavaTags), clock))
  }

  override def timer(name: String, tags: Tag*): F[Timer[F]] = timer(name, tags)

  override def gauge[A: ToDouble](name: String, tags: Iterable[Tag])(retrieveValue: () => A): F[Gauge[F]] = {
    val conv = implicitly[ToDouble[A]]

    F.delay {
      new DefaultGauge(
        JavaGauge.builder(name, retrieveValue, (value: () => A) => conv.toDouble(value())).tags(tags.asJavaTags).register(delegate)
      )
    }
  }

  override def gauge[A: ToDouble](name: String)(retrieveValue: () => A): F[Gauge[F]] = gauge(name, List.empty)(retrieveValue)

  override def gaugeCollectionSize[A <: Iterable[_]](name: String, tags: Iterable[Tag], collection: A): F[Gauge[F]] = {
    gauge(name, tags)(() => collection)(CollectionSizeToDouble)
  }

  override def timerPair(name: String, tags: Iterable[Tag]): F[TimerPair[F]] = ???

  override def timerPair(name: String, tags: Tag*): F[TimerPair[F]] = ???

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
