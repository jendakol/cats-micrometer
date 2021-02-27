import sbt.Keys._
import sbt._

object BuildSettings {

  lazy val scala212 = "2.12.13"
  lazy val scala213 = "2.13.4"

  lazy val common: Seq[Def.Setting[_]] = Seq(
    ThisBuild / scalaVersion := scala213,
    crossScalaVersions := List(scala212, scala213),
    fork := true,
    libraryDependencies ++= Seq(
      compilerPlugin(Dependencies.kindProjector),
      Dependencies.catsEffect,
      Dependencies.scalaCollectionCompat,
      Dependencies.scalaTest % Test
    ),
    Test / publishArtifact := false
  )

}
