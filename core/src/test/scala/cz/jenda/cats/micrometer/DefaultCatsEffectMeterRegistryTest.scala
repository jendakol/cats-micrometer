package cz.jenda.cats.micrometer

import cats.effect.IO
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.scalatest.funsuite.AsyncFunSuite

import java.util.concurrent.TimeUnit.MILLISECONDS
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

class DefaultCatsEffectMeterRegistryTest extends AsyncFunSuite {

  test("basic") {
    val javaRegistry = new SimpleMeterRegistry()
    val scalaRegistry = DefaultCatsEffectMeterRegistry.wrap[IO](javaRegistry).allocated.unsafeRunSync()._1

    val io = for {
      counter <- scalaRegistry.counter("test-counter")
      _ <- counter.increment
      count <- counter.count
      gauge <- scalaRegistry.gauge("test-gauge")(() => 123)
      gaugeValue <- gauge.value
      timer <- scalaRegistry.timer("test-timer")
      _ <- timer.wrap(IO.sleep(Duration(350, MILLISECONDS))(IO.timer(ExecutionContext.global)))
      time <- timer.totalTime(MILLISECONDS)
    } yield assert(count === 1.0 && gaugeValue === 123.0 && time >= 300 && time <= 400)

    io.unsafeToFuture()
  }

}
