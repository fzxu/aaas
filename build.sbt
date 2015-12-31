name := "aaas"

version := "1.0"

scalaVersion := "2.11.7"

val phantomVersion = "1.12.2"
val akkaHttpVersion = "2.0.1"
val json4sVersion = "3.3.0"
val slf4jVersion = "1.7.13"

resolvers ++= Seq(
  "Typesafe repository snapshots"    at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe repository releases"     at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
  "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
  "Twitter Repository"               at "http://maven.twttr.com",
  Resolver.bintrayRepo("websudos", "oss-releases")
)

libraryDependencies ++= Seq(
  "com.websudos" %% "phantom-dsl" % phantomVersion,
  "junit" % "junit" % "4.12",
  "org.scalatest" %% "scalatest" % "2.2.5",
  "commons-io" % "commons-io" % "2.4",
  "com.typesafe.akka" %% "akka-http-experimental" % akkaHttpVersion,
  "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.0",
  "org.json4s" %% "json4s-jackson" % json4sVersion,
  "org.json4s" %% "json4s-ext" % json4sVersion,
  "org.json4s" %% "json4s-native" % json4sVersion,
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion
)

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}