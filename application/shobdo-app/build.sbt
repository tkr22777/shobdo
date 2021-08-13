name := """shobdo-app"""

version := "0.11-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies += "redis.clients" % "jedis" % "1.5.2"

libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.5"

libraryDependencies += "org.mongodb" % "mongo-java-driver" % "3.2.2"

libraryDependencies += "org.projectlombok" % "lombok" % "1.16.16"

libraryDependencies += "org.mockito" % "mockito-core" % "2.5.0"

libraryDependencies += "com.google.guava" % "guava" % "21.0"

libraryDependencies += filters

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs
)
