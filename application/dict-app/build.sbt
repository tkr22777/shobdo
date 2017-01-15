name := """dict-app"""

version := "0.11-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies += "redis.clients" % "jedis" % "1.5.2"

libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.5"

libraryDependencies += "org.mongodb" % "mongo-java-driver" % "3.2.2"

libraryDependencies += "org.mockito" % "mockito-core" % "2.5.0"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs
)
