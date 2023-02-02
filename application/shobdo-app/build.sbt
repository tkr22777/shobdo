name := """shobdo-app"""

version := "0.11-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies += "redis.clients" % "jedis" % "1.5.2"

libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.5"

libraryDependencies += "org.mongodb" % "mongo-java-driver" % "3.12.11"

libraryDependencies += "org.projectlombok" % "lombok" % "1.16.16"

libraryDependencies += "org.mockito" % "mockito-core" % "2.5.0"

libraryDependencies += "com.google.guava" % "guava" % "21.0"

// following libs are to migrate from google sheets
libraryDependencies += "com.google.api-client" % "google-api-client" % "2.0.0"
libraryDependencies += "com.google.oauth-client" % "google-oauth-client-jetty" % "1.34.1"
libraryDependencies += "com.google.apis" % "google-api-services-sheets" % "v4-rev20220927-2.0.0"

libraryDependencies += filters

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs
)
