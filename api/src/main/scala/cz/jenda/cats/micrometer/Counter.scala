package cz.jenda.cats.micrometer

trait Counter[F[_]] {
  def increment: F[Unit]

  def increment(amount: Double): F[Unit]

  def count: F[Double]
}
