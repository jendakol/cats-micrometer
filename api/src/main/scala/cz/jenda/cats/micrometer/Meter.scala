package cz.jenda.cats.micrometer

trait Meter {
  def id: MeterId
  def measurements: Set[Measurement]
}

case class Tag(key: String, value: String)

case class MeterId(name: String, tags: Seq[Tag], `type`: MeterType, description: Option[String], baseUnit: Option[String])

trait Measurement {
  def value(): Double
  def statistic: Statistic
}

sealed abstract class Statistic(tagValue: String)

object Statistic {
  case object Total extends Statistic("total")
  case object TotalTime extends Statistic("total")
  case object Count extends Statistic("count")
  case object Max extends Statistic("max")
  case object Value extends Statistic("value")
  case object Unknown extends Statistic("unknown")
  case object ActiveTasks extends Statistic("active")
  case object Duration extends Statistic("duration")
}

sealed trait MeterType

object MeterType {
  case object Counter extends MeterType
  case object Gauge extends MeterType
  case object LongTaskTimer extends MeterType
  case object Timer extends MeterType
  case object DistributionSummary extends MeterType
  case object Other extends MeterType
}
