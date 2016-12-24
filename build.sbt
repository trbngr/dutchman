import Dependencies._
import com.amazonaws.services.s3.model.Region

lazy val buildSettings = Seq(
  version := "0.5.3",
  organization := "com.caliberweb",
  name := "dutchman",
  description := "Dutchman",
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-Xlint:-infer-any", "-Xfatal-warnings", "-language:postfixOps", "-language:implicitConversions"),
  testOptions in Test += Tests.Argument("-oD"),

  //Let CTRL+C kill the current task and not the whole SBT session.
  cancelable in Global := true
)

lazy val publishSettings = Seq(
  s3region := Region.US_West_2,
  s3overwrite := true,
  publishArtifact in Test := false,
  pomIncludeRepository := (_ ⇒ true),
  publishMavenStyle := true,
  publishTo := {
    val folder = if (isSnapshot.value) "snapshot" else "release"
    Some(s3resolver.value("caliberweb repo", s3(s"repo.caliberweb.com/$folder")) withMavenPatterns)
  }
)

lazy val dutchman = project.in(file("."))
  .settings(buildSettings: _*)
  .settings(
    moduleName := "dutchman-client",
    publishArtifact := false,
    publish := {}
  )
  .aggregate(core, aws, circe, test, akka)

lazy val core = project.in(file("core"))
  .settings(buildSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    moduleName := "dutchman-core",
    libraryDependencies ++= Seq(slf4j)
  )

lazy val test = project.in(file("test"))
  .settings(buildSettings: _*)
  .settings(
    moduleName := "dutchman-test",
    libraryDependencies ++= Seq(scalaTest),
    libraryDependencies ++= Seq(elasticsearch),
    publish := {}
  )
  .dependsOn(core)

lazy val akka = project.in(file("akka"))
  .settings(buildSettings: _*)
  .settings(
    moduleName := "dutchman-akka",
    libraryDependencies ++= Seq(akkaHttp, akkaStream, akkaSlf4j, akkaTestKit)
  )
  .dependsOn(core, test % "test", circe % "test")
  .settings(publishSettings: _*)

lazy val aws = project.in(file("aws"))
  .settings(buildSettings: _*)
  .settings(
    moduleName := "dutchman-aws",
    libraryDependencies ++= Seq(awsSdkCore),
    libraryDependencies ++= Seq(scalaTest % "test")
  )
  .dependsOn(core)
  .settings(publishSettings: _*)

lazy val circe = project.in(file("circe"))
  .settings(buildSettings: _*)
  .settings(
    moduleName := "dutchman-circe",
    libraryDependencies ++= Dependencies.circe,
    libraryDependencies ++= Seq(slf4j, scalaTest % "test")
  )
  .dependsOn(core, test % "test")
  .settings(publishSettings: _*)