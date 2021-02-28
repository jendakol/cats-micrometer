package cz.jenda.cats.micrometer

import io.micrometer.core.instrument.{Tags, Measurement => JavaMeasurement, Meter => JavaMeter, Statistic => JavaStatistic, Tag => JavaTag}

import java.lang.{Iterable => JavaIterable}
import scala.jdk.CollectionConverters._

private[micrometer] object MicrometerJavaConverters {
  implicit class ScalaTagsConverter(val tags: Iterable[Tag]) extends AnyVal {
    def asJavaTags: JavaIterable[JavaTag] = {
      tags.map { t =>
        new JavaTag {
          override def getKey: String = t.key
          override def getValue: String = t.value
        }
      }.asJava
    }
  }

  implicit class ScalaMeterIdConverter(val id: MeterId) extends AnyVal {
    def asJava: JavaMeter.Id = {
      new JavaMeter.Id(id.name, Tags.of(id.tags.asJavaTags), id.baseUnit.orNull, id.description.orNull, id.`type`.asJava)
    }
  }

  implicit class ScalaMeterTypeConverter(val t: MeterType) extends AnyVal {
    def asJava: JavaMeter.Type = {
      import JavaMeter.Type
      t match {
        case MeterType.Counter             => Type.COUNTER
        case MeterType.Gauge               => Type.GAUGE
        case MeterType.LongTaskTimer       => Type.LONG_TASK_TIMER
        case MeterType.Timer               => Type.TIMER
        case MeterType.DistributionSummary => Type.DISTRIBUTION_SUMMARY
        case MeterType.Other               => Type.OTHER
      }
    }
  }

  implicit class JavaTagsConverter(val tags: JavaIterable[JavaTag]) extends AnyVal {
    def asScalaTags: Seq[Tag] = {
      tags.asScala.map { t =>
        Tag(t.getKey, t.getValue)
      }.toSeq
    }
  }

  implicit class JavaMeterIdConverter(val id: JavaMeter.Id) extends AnyVal {
    def asScala: MeterId = {
      MeterId(id.getName, id.getTagsAsIterable.asScalaTags, id.getType.asScala, Option(id.getDescription), Option(id.getBaseUnit))
    }
  }

  implicit class JavaMeterTypeConverter(val t: JavaMeter.Type) extends AnyVal {
    def asScala: MeterType = {
      import JavaMeter.Type
      t match {
        case Type.COUNTER              => MeterType.Counter
        case Type.GAUGE                => MeterType.Gauge
        case Type.LONG_TASK_TIMER      => MeterType.LongTaskTimer
        case Type.TIMER                => MeterType.Timer
        case Type.DISTRIBUTION_SUMMARY => MeterType.DistributionSummary
        case Type.OTHER                => MeterType.Other
      }
    }
  }

  implicit class JavaMeasurementsConverte(val ms: JavaIterable[JavaMeasurement]) extends AnyVal {
    def asScalaMeasurements: Set[Measurement] = {
      ms.asScala.map { t =>
        new Measurement {
          override def value(): Double = t.getValue

          override def statistic: Statistic = t.getStatistic.asScala
        }
      }.toSet
    }
  }

  implicit class JavaStatisticsConverter(val stat: JavaStatistic) extends AnyVal {
    def asScala: Statistic = {
      stat match {
        case JavaStatistic.TOTAL        => Statistic.Total
        case JavaStatistic.TOTAL_TIME   => Statistic.TotalTime
        case JavaStatistic.COUNT        => Statistic.Count
        case JavaStatistic.MAX          => Statistic.Max
        case JavaStatistic.VALUE        => Statistic.Value
        case JavaStatistic.UNKNOWN      => Statistic.Unknown
        case JavaStatistic.ACTIVE_TASKS => Statistic.ActiveTasks
        case JavaStatistic.DURATION     => Statistic.Duration
      }
    }
  }

  implicit class JavaMeterConverter(val meter: JavaMeter) extends AnyVal {
    def asScala: Meter = {
      new Meter {
        override def id: MeterId = meter.getId.asScala
        override def measurements: Set[Measurement] = meter.measure().asScalaMeasurements
      }
    }
  }

  implicit class JavaMetersConverter(val meters: JavaIterable[JavaMeter]) extends AnyVal {
    def asScalaMeters: Seq[Meter] = {
      meters.asScala.map(_.asScala).toSeq
    }
  }
}
