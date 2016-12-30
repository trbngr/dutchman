import sbt._

object Dependencies {

  object Version {
    val akka = "2.4.14"
    val scalaTest = "3.0.1"
    val circe = "0.6.1"
    val logback = "1.1.7"
    val awsSdkCore = "1.11.66"
    val json4s = "3.5.0"
    val elasticsearch = "2.4.3"
    val slf4j = "1.7.16"
    val cats = "0.8.1"
  }

  private def akkaModule(module: String, version: String = Version.akka) = "com.typesafe.akka" %% s"akka-$module" % version

  val akkaActor = akkaModule("actor")
  val akkaSlf4j = akkaModule("slf4j")
  val akkaStream = akkaModule("stream")
  val akkaTestKit = akkaModule("testkit") % "test"
  val akkaHttp = akkaModule("http", "10.0.0")
  val slf4j = "org.slf4j" % "slf4j-api" % Version.slf4j

  val logback = "ch.qos.logback" % "logback-classic" % Version.logback
  val logbackJackson = "co.wrisk.logback" % "logback-ext-jackson" % "1.0.3"
  val logbackCloudWatch = "co.wrisk.logback" % "logback-ext-cloudwatch-appender" % "1.0.3"

  val awsSdkCore = "com.amazonaws" % "aws-java-sdk-core" % Version.awsSdkCore
  val json4s = "org.json4s" %% "json4s-native" % Version.json4s
  val cats = "org.typelevel" %% "cats-free" % Version.cats

  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest
  val elasticsearch = "org.elasticsearch" % "elasticsearch" % Version.elasticsearch

  val circe = Seq("core", "generic", "parser"
  ) map (x ⇒ "io.circe" %% s"circe-$x" % Version.circe)
}