package cz.jenda.cats

import io.micrometer.core.instrument.{Tag => JavaTag}

import scala.jdk.CollectionConverters._
package object micrometer {
  private[micrometer] implicit class TagsConverters(val tags: Iterable[Tag]) extends AnyVal {
    def asJavaTags: java.lang.Iterable[JavaTag] = {
      tags.map { t =>
        new JavaTag {
          override def getKey: String = t.key

          override def getValue: String = t.value
        }
      }.asJava
    }
  }
}
