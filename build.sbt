
name := "spark-text"

scalaVersion := "2.10.4"

val kuromojiVersion     = "0.7.7"
val sparkCsvVersion     = "1.3.0"
val sparkVersion        = "1.5.2"
val slf4jVersion        = "1.6.1"
val logbackVersion      = "1.1.1"
val scalaTestVersion    = "2.2.4"
val mockitoVersion      = "1.9.5"

resolvers ++= Seq(
  "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository",
  "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven",
  "Atilika Open Source repository" at "http://www.atilika.org/nexus/content/repositories/atilika"
)

libraryDependencies ++= Seq(
  "org.apache.spark"     %% "spark-core"      % sparkVersion     % "provided",
  "org.apache.spark"     %% "spark-mllib"     % sparkVersion     % "provided",
  "org.apache.spark"     %% "spark-sql"       % sparkVersion     % "provided",

  "org.atilika.kuromoji" %  "kuromoji"        % kuromojiVersion,
  "com.databricks"       %% "spark-csv"       % sparkCsvVersion,

  "org.slf4j"            %  "slf4j-api"       % slf4jVersion,
  "ch.qos.logback"       %  "logback-classic" % logbackVersion   % "test",
  "org.scalatest"        %% "scalatest"       % scalaTestVersion % "test",
  "org.mockito"          %  "mockito-all"     % mockitoVersion   % "test"
)

scalariformSettings
