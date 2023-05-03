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
      "dev.zio"       %% "zio-test-sbt"          % "2.0.13" % Test,
      "ch.qos.logback" % "logback-classic"       % "1.4.7"  % Test,
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  )

lazy val m6 = (project in file("m6"))
  .settings(
    name := "temporal-either-repro-m6",
    libraryDependencies ++= Seq(
      "dev.vhonta"    %% "zio-temporal-core"     % "0.2.0-M6",
      "dev.vhonta"    %% "zio-temporal-protobuf" % "0.2.0-M6",
      "dev.vhonta"    %% "zio-temporal-testkit"  % "0.2.0-M6",
      "dev.zio"       %% "zio-test"              % "2.0.13" % Test,
      "dev.zio"       %% "zio-test-sbt"          % "2.0.13" % Test,
      "ch.qos.logback" % "logback-classic"       % "1.4.7"  % Test,
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  )

lazy val v1 = (project in file("v1"))
  .settings(
    name := "temporal-either-repro-v1",
    libraryDependencies ++= Seq(
      "dev.vhonta"    %% "zio-temporal-core"     % "0.1.0-RC6",
      "dev.vhonta"    %% "zio-temporal-protobuf" % "0.1.0-RC6",
      "dev.vhonta"    %% "zio-temporal-testkit"  % "0.1.0-RC6",
      "dev.zio"       %% "zio-test"              % "2.0.13" % Test,
      "dev.zio"       %% "zio-test-sbt"          % "2.0.13" % Test,
      "ch.qos.logback" % "logback-classic"       % "1.4.7"  % Test,
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  )