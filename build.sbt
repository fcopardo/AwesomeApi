name := "spray-api"

version := "1.0"

organization := "com.bbr"

scalaVersion  := "2.10.4"

scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "mvn repository" at "http://mvnrepository.com/artifact/",
  "mvn central repo" at "http://repo1.maven.org/maven2/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

// Framework
libraryDependencies ++= Seq(
    "com.typesafe"              %   "config"                    % "1.2.1",
    "com.typesafe.akka"         %%  "akka-actor"                % "2.3.2",
    "io.spray"                  %   "spray-can"                 % "1.3.1",
    "io.spray"                  %   "spray-routing"             % "1.3.1"
)

// Storage and DB
libraryDependencies ++= Seq(
  "mysql"                       %   "mysql-connector-java"      % "5.1.30",
  "com.jolbox"                  %   "bonecp"                    % "0.8.0.RELEASE",
  "com.typesafe.slick"          %%  "slick"                     % "2.1.0-RC1",
  "org.reactivemongo"           %%  "reactivemongo"             % "0.10.0"
)

// Utilities
libraryDependencies ++= Seq(
  "com.restfb"                  %   "restfb"                    % "1.6.14",
  "net.databinder.dispatch"     %%  "dispatch-core"             % "0.11.1",
  "org.apache.directory.studio" %   "org.apache.commons.codec"  % "1.8",
  "org.json4s"                  %%  "json4s-native"             % "3.2.10",
  "com.rabbitmq"                %   "amqp-client"               % "3.3.4"
)

// Debugging & Testing
libraryDependencies ++= Seq(
  "io.spray"                    %   "spray-testkit"             % "1.3.1",
  "org.specs2"                  %%  "specs2"                    % "2.3.12"    % "test",
  "ch.qos.logback"              %   "logback-classic"           % "1.1.2"
)

// Monitoring
libraryDependencies ++= Seq(
  "io.kamon"                    %%  "kamon-core"                % "0.3.1",
  "io.kamon"                    %%  "kamon-spray"               % "0.3.1"
)

org.scalastyle.sbt.ScalastylePlugin.Settings
