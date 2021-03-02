ThisBuild / organization := "cz.jenda.cats-micrometer"
ThisBuild / turbo := true
Global / onChangedBuildSource := ReloadOnSourceChanges
Global / cancelable := true

lazy val root = project
  .in(file("."))
  .aggregate(api, core)
  .settings(
    name := "cats-micrometer",
    publish / skip := true
  )

lazy val api = project
  .in(file("api"))
  .settings(BuildSettings.common)
  .settings(
    name := "cats-micrometer-api",
    libraryDependencies ++= Seq(Dependencies.micrometerCore)
  )

lazy val core = project
  .in(file("core"))
  .settings(BuildSettings.common)
  .settings(
    name := "cats-micrometer-core",
    libraryDependencies ++= Seq(Dependencies.catsEffect)
  )
  .dependsOn(api)

addCommandAlias("lint", "; scalafmtSbtCheck; scalafmtCheckAll")
addCommandAlias("check", "; lint; +missinglinkCheck; scalafmtSbtCheck; scalafmtCheckAll; +test")
