ThisBuild / organization := "cz.jenda.cats-micrometer"
ThisBuild / turbo := true
Global / onChangedBuildSource := ReloadOnSourceChanges
Global / cancelable := true

lazy val root = project
  .in(file("."))
  .aggregate(api)
  .settings(
    name := "cats-micrometer",
    publish / skip := true
  )

lazy val api = project
  .in(file("api"))
  .settings(BuildSettings.common)
  .settings(
    name := "cats-micrometer-api",
    libraryDependencies ++= Seq(
      Dependencies.catsEffect,
      Dependencies.jsr305,
      Dependencies.micrometerCore
    )
  )

addCommandAlias("check", "; scalafmtSbtCheck; scalafmtCheckAll; +test")
addCommandAlias("fix", "; scalafmtSbt; scalafmtAll")
