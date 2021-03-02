package cz.jenda.cats.micrometer

import cats.effect.IO
import cats.syntax.all._
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.ExecutionContext.Implicits.global

class DefaultCatsMeterRegistryTest extends AsyncFunSuite {

//  test("basic") {
//    val javaRegistry = new SimpleMeterRegistry()
//    val scalaRegistry = DefaultCatsMeterRegistry.wrap[IO](javaRegistry).allocated.unsafeRunSync()._1
//
//    val io = for {
//      counter <- scalaRegistry.counter("test-counter")
//      _ <- counter.increment
//      count <- counter.count
//      gauge <- scalaRegistry.gauge("test-gauge")(() => 123)
//      gaugeValue <- gauge.value
//      timer <- scalaRegistry.timer("test-timer")
//      _ <- timer.wrap(IO.sleep(Duration(350, MILLISECONDS))(IO.timer(ExecutionContext.global)))
//      time <- timer.totalTime(MILLISECONDS)
//    } yield assert(count === 1.0 && gaugeValue === 123.0 && time >= 300 && time <= 400)
//
//    io.unsafeToFuture()
//  }

  test("resource") {
    class MyClass(metrics: CatsMeterRegistry[IO]) {
      def doTheJob(): IO[Unit] = {
        metrics.timer("theJob").use { timer =>
          timer.wrap {
            IO.delay(println("doing theJob"))
          }
        }
      }
    }

    DefaultCatsMeterRegistry
      .wrap[IO](new SimpleMeterRegistry())
      .map(new MyClass(_))
      .use { myClass =>
        myClass.doTheJob() >> IO.delay(assert(1 == 1))
      }
      .unsafeToFuture()
  }
}
