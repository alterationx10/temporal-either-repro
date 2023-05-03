ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "temporal-either-repro",
    libraryDependencies ++= Seq(
      "dev.vhonta"    %% "zio-temporal-core"     % "0.2.0-RC1",
      "dev.vhonta"    %% "zio-temporal-protobuf" % "0.2.0-RC1",
      "dev.vhonta"    %% "zio-temporal-testkit"  % "0.2.0-RC1",
      "dev.zio"       %% "zio-test"              % "2.0.13" % Test,
      "ch.qos.logback" % "logback-classic"       % "1.4.7"  % Test,
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  )
