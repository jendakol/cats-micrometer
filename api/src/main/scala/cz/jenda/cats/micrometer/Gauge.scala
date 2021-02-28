package cz.jenda.cats.micrometer

trait Gauge[F[_]] extends Meter {
  def value: F[Double]
}
