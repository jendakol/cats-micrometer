package cz.jenda.cats.micrometer

trait MeterRegistry[F[_]] {

//  def meters: F[Seq[Meter]]
//
//  def foreach(f: Meter => Unit): Unit
//
//  def config: instrument.MeterRegistry#Config
//
//  def find(name: String): F[Option[Search]]
//
//  def get(name: String): F[RequiredSearch]

  def counter(name: String, tags: Iterable[Tag]): F[Counter[F]]

  def counter(name: String, tags: Tag*): F[Counter[F]]

//  def summary(name: String, tags: Iterable[Tag]): F[DistributionSummary]
//
//  def summary(name: String, tags: Tag*): F[DistributionSummary]

  def timer(name: String, tags: Iterable[Tag]): F[Timer[F]]

  def timer(name: String, tags: Tag*): F[Timer[F]]

  def gauge[A: ToDouble](name: String, tags: Iterable[Tag], numberLike: A): F[Gauge[F]]

  def gauge[A: ToDouble](name: String, numberLike: A): F[Gauge[F]]

  def gaugeCollectionSize[A <: Iterable[_]](name: String, tags: Iterable[Tag], collection: A): F[Gauge[F]]

//  def remove(meter: Meter): F[Option[Meter]]
//
//  def remove(meterId: Meter.Id): F[Option[Meter]]

  def clear: F[Unit]
}
