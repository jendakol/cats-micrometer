package cz.jenda.cats.micrometer

import cats.effect.Resource
import io.micrometer.core.instrument.{MeterRegistry => JavaMeterRegistry}

trait CatsMeterRegistry[F[_]] {

  def underlying: JavaMeterRegistry

  def meters: F[Seq[Meter]]

//
//  def config: instrument.MeterRegistry#Config
//
//  def find(name: String): F[Option[Search]]
//
//  def get(name: String): F[RequiredSearch]

  def counter(name: String, tags: Iterable[Tag]): Resource[F, Counter[F]]

  def counter(name: String, tags: Tag*): Resource[F, Counter[F]]

//  def summary(name: String, tags: Iterable[Tag]): F[DistributionSummary]
//
//  def summary(name: String, tags: Tag*): F[DistributionSummary]

  def timer(name: String, tags: Iterable[Tag]): Resource[F, Timer[F]]

  def timer(name: String, tags: Tag*): Resource[F, Timer[F]]

  def timerPair(name: String, tags: Iterable[Tag]): Resource[F, TimerPair[F]]

  def timerPair(name: String, tags: Tag*): Resource[F, TimerPair[F]]

  def gauge[A: ToDouble](name: String, tags: Iterable[Tag])(retrieveValue: () => A): Resource[F, Gauge[F]]

  def gauge[A: ToDouble](name: String)(retrieveValue: () => A): Resource[F, Gauge[F]]

  def gaugeCollectionSize[A <: Iterable[_]](name: String, tags: Iterable[Tag], collection: A): Resource[F, Gauge[F]]

  def remove(meter: Meter): F[Option[Meter]]

  def remove(meterId: MeterId): F[Option[Meter]]

  def clear: F[Unit]
}
