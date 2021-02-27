package cz.jenda.cats.micrometer

trait ToDouble[-A] {

  def toDouble(value: A): Double

}

object ToDouble {

  def apply[A](implicit ev: ToDouble[A]): ToDouble[A] = ev

  implicit object IntToDouble extends ToDouble[Int] {
    override def toDouble(value: Int): Double = value.toDouble
  }

  implicit object LongToDouble extends ToDouble[Long] {
    override def toDouble(value: Long): Double = value.toDouble
  }

  implicit object NumberToDouble extends ToDouble[Number] {
    override def toDouble(value: Number): Double = value.doubleValue()
  }

}
