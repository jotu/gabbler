import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform._

object Build extends Build {

  lazy val gabbler = Project(
    "gabbler",
    file("."),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
        Dependency.Compile.akkaActor,
        Dependency.Compile.akkaSlf4j,
        Dependency.Compile.logbackClassic,
        Dependency.Compile.sprayCan,
        Dependency.Compile.sprayJson,
        Dependency.Compile.sprayRouting
      )
    )
  )

  def commonSettings =
    Defaults.defaultSettings ++ 
    scalariformSettings ++
    Seq(
      organization := "name.heikoseeberger",
      // version is defined in version.sbt to support sbt-release
      scalaVersion := Version.scala,
      scalacOptions ++= Seq(
        "-unchecked",
        "-deprecation",
        "-Xlint",
        "-language:_", 
        "-target:jvm-1.7",
        "-encoding", "UTF-8"
      ),
      resolvers += "spray repo" at "http://repo.spray.io",
      libraryDependencies ++= Seq(
        Dependency.Test.akkaTestkit,
        Dependency.Test.scalaTest,
        Dependency.Test.scalaCheck,
        Dependency.Test.scalaMock,
        Dependency.Test.sprayTestkit
      ),
      initialCommands in console := "import name.heikoseeberger.gabbler._"
    )

  object Version {
    val akka = "2.1.0"
    val scala = "2.10.1-RC3"
    val spray = "1.1-M7"
  }

  object Dependency {

    object Compile {
      val akkaActor = "com.typesafe.akka" %% "akka-actor" % Version.akka
      val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % Version.akka
      val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.0.9"
      val sprayCan = "io.spray" % "spray-can" % Version.spray
      val sprayJson = "io.spray" %% "spray-json" % "1.2.3"
      val sprayRouting = "io.spray" % "spray-routing" % Version.spray
    }

    object Test {
      val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % Version.akka % "test"
      val scalaTest = "org.scalatest" %% "scalatest" % "2.0.M6-SNAP8" % "test"
      val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.10.0" % "test"
      val scalaMock = "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test"
      val sprayTestkit = "io.spray" % "spray-testkit" % Version.spray % "test"
    }
  }
}
