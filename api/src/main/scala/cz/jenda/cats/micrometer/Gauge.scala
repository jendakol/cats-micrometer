package cz.jenda.cats.micrometer

trait Gauge[F[_]] {
  def value: F[Double]
}
