import sbt._

object Dependencies {

  object Version {
    val akka = "2.4.16"
    val akkaHttp = "10.0.1"
    val scalaTest = "3.0.1"
    val circe = "0.6.1"
    val awsSdkCore = "1.11.66"
    val slf4j = "1.7.16"
    val cats = "0.8.1"
  }

  val slf4j = "org.slf4j" % "slf4j-api" % Version.slf4j
  val cats = "org.typelevel" %% "cats-free" % Version.cats

  val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % Version.akka % Test
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.akkaHttp
  val awsSdkCore = "com.amazonaws" % "aws-java-sdk-core" % Version.awsSdkCore
  val circe = Seq("core", "generic", "parser"
  ) map (x ⇒ "io.circe" %% s"circe-$x" % Version.circe)
}