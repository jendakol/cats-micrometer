package cz.jenda.cats.micrometer
import cz.jenda.cats.micrometer.MicrometerJavaConverters._
import io.micrometer.core.instrument.{Meter => JavaMeter}

abstract class DefaultMeter extends Meter {
  protected def delegate: JavaMeter

  override def id: MeterId = delegate.getId.asScala
  override def measurements: Set[Measurement] = delegate.measure().asScalaMeasurements

}
