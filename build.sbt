name := "designated-director"

version := "0.1"

scalaVersion := "2.12.6"

lazy val akkaHttpVersion = "10.1.1"
lazy val akkaVersion    = "2.5.11"

lazy val api = (project in file ("./api"))
  .settings(
    inThisBuild(List(
      organization := "designated.director",
      scalaVersion := "2.12.6"
    )),
    name := "designated-director-api",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "com.github.swagger-akka-http" %% "swagger-akka-http" % "0.14.0",

      /* Java Libraries */
      "org.neo4j.driver" % "neo4j-java-driver" % "1.6.1",

      /* Test */
      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.1"         % Test
    ),
    mainClass in (Compile, run) := Some("designated.director.api.WebServer")
  )
