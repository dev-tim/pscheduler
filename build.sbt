import com.banno.license.Licenses._
import com.banno.license.Plugin.LicenseKeys._
import net.virtualvoid.sbt.graph.Plugin._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

val scalaV = "2.11.7"

val commonSettings =
  graphSettings ++
  licenseSettings ++
  Seq(
    organization  := "pl.touk.pscheduler",
    scalaVersion  := scalaV,
    scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    license := apache2("Copyright 2015 the original author or authors."),
    licenses :=  Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    homepage := Some(url("https://github.com/touk/pscheduler")),
    removeExistingHeaderBlock := true,
    resolvers ++= Seq(
      "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
    )
  )

val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pomExtra in Global := {
    <scm>
      <connection>scm:git:github.com/touk/pscheduler.git</connection>
      <developerConnection>scm:git:git@github.com:touk/pscheduler.git</developerConnection>
      <url>github.com/touk/pscheduler</url>
    </scm>
    <developers>
      <developer>
        <id>ark_adius</id>
        <name>Arek Burdach</name>
        <url>https://github.com/arkadius</url>
      </developer>
    </developers>
  }
)

val akkaV = "2.4.0"
val slf4jV = "1.7.13"
val logbackV = "1.1.3"
val scalaTestV = "3.0.0-M9"
val scalacheckV = "1.12.5"
val slickV = "3.0.3"
val flywayV = "3.2.1"
val hsqldbV = "2.3.3"
val hikariV = "2.4.2"

lazy val core = (project in file("pscheduler-core")).
  settings(commonSettings).
  settings(publishSettings).
  settings(
    name := "pscheduler-core",
    libraryDependencies ++= {
      Seq(
        "org.slf4j"                 % "slf4j-api"                     % slf4jV,
        "org.scalatest"            %% "scalatest"                     % scalaTestV    % "test",
        "ch.qos.logback"            % "logback-classic"               % logbackV      % "test",
        "org.scalacheck"           %% "scalacheck"                    % scalacheckV   % "test"
      )
    }
  )

lazy val akkaScheduler = (project in file("pscheduler-akka")).
  settings(commonSettings).
  settings(publishSettings).
  settings(
    name := "pscheduler-akka",
    libraryDependencies ++= {
      Seq(
        "com.typesafe.akka"        %% "akka-actor"                    % akkaV,
        "org.scalatest"            %% "scalatest"                     % scalaTestV    % "test",
        "ch.qos.logback"            % "logback-classic"               % logbackV      % "test"
      )
    }
  ).
  dependsOn(core)

lazy val slickStore = (project in file("pscheduler-slick-store")).
  settings(commonSettings).
  settings(publishSettings).
  settings(
    name := "pscheduler-slick-store",
    libraryDependencies ++= {
      Seq(
        "com.typesafe.slick"       %% "slick"                         % slickV,
        "org.flywaydb"              % "flyway-core"                   % flywayV,
        "org.scalatest"            %% "scalatest"                     % scalaTestV    % "test",
        "com.typesafe.slick"       %% "slick-testkit"                 % slickV        % "test",
        "org.hsqldb"                % "hsqldb"                        % hsqldbV       % "test",
        "com.zaxxer"                % "HikariCP"                      % hikariV       % "test"
      )
    }
  ).
  dependsOn(core)

publishArtifact := false

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _)),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
  pushChanges
)
