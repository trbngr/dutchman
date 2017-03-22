import sbt._

object Dependencies {

  object Version {
    val akka = "2.5.0-RC1"
    val akkaHttp = "10.0.5"
    val scalaTest = "3.0.1"
    val circe = "0.7.0"
    val awsSdkCore = "1.11.105"
    val slf4j = "1.7.25"
    val cats = "0.9.0"
    val sprayJson = "1.3.3"
  }

  val slf4j = "org.slf4j" % "slf4j-api" % Version.slf4j
  val cats = "org.typelevel" %% "cats-free" % Version.cats

  val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % Version.akka % Test
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.akkaHttp
  val awsSdkCore = "com.amazonaws" % "aws-java-sdk-core" % Version.awsSdkCore
  val sprayJson = "io.spray" %% "spray-json" % Version.sprayJson
  val circe = Seq("core", "generic", "parser"
  ) map (x ⇒ "io.circe" %% s"circe-$x" % Version.circe)
}