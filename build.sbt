import Dependencies._
import com.amazonaws.services.s3.model.Region

lazy val buildSettings = Seq(
  version := "0.2.9",
  organization := "com.caliberweb",
  name := "dutchman",
  description := "Dutchman",
  scalaVersion := "2.12.1",
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  scalacOptions := Seq(
    "-encoding", "UTF-8",
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Ywarn-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-unused",
    "-Xlint:-infer-any",
    "-Xfatal-warnings",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:higherKinds"
  ),
  testOptions in Test += Tests.Argument("-oD"),

  EclipseKeys.skipParents in ThisBuild := true,

  mappings in (Compile, packageSrc) ++= {
    val base  = (sourceManaged  in Compile).value
    val files = (managedSources in Compile).value
    files.map { f => (f, f.relativeTo(base).get.getPath) }
  },

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
    name := "dutchman-client",
    moduleName := "dutchman-client",
    publishArtifact := false,
    publish := {}
  )
  .aggregate(core, aws, circe, sprayJson, test, akka)

lazy val core = project.in(file("core"))
  .settings(buildSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "dutchman-core",  
    moduleName := "dutchman-core",
    libraryDependencies ++= Seq(cats, slf4j),
    libraryDependencies ++= Seq(scalaTest % Test)
  )

lazy val test = project.in(file("test"))
  .settings(buildSettings: _*)
  .settings(
    name := "dutchman-test",  
    moduleName := "dutchman-test",
    libraryDependencies ++= Seq(scalaTest),
    publish := {}
  )
  .dependsOn(core % "compile,test")

lazy val akka = project.in(file("akka"))
  .settings(buildSettings: _*)
  .settings(
    name := "dutchman-akka",  
    moduleName := "dutchman-akka",
    libraryDependencies ++= Seq(akkaHttp, akkaTestKit)
  )
  .dependsOn(core, test % Test, circe % Test)
  .settings(publishSettings: _*)

lazy val aws = project.in(file("aws"))
  .settings(buildSettings: _*)
  .settings(
    name := "dutchman-aws",    
    moduleName := "dutchman-aws",
    libraryDependencies ++= Seq(awsSdkCore),
    libraryDependencies ++= Seq(scalaTest % Test)
  )
  .dependsOn(core)
  .settings(publishSettings: _*)

lazy val circe = project.in(file("circe"))
  .settings(buildSettings: _*)
  .settings(
    name := "dutchman-circe",      
    moduleName := "dutchman-circe",
    libraryDependencies ++= Dependencies.circe,
    libraryDependencies ++= Seq(slf4j, scalaTest % Test)
  )
  .dependsOn(core, test % Test)
  .settings(publishSettings: _*)

lazy val sprayJson = project.in(file("sprayjson"))
  .settings(buildSettings: _*)
  .settings(
    name := "dutchman-sprayjson",
    moduleName := "dutchman-sprayjson",
    libraryDependencies ++= Seq(Dependencies.sprayJson),
    libraryDependencies ++= Seq(slf4j, scalaTest % Test)
  )
  .dependsOn(core, test % Test)
  .settings(publishSettings: _*)
