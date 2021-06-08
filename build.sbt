name := """transaction_manager"""
organization := "com.abhinav"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.6"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies ++=
  Seq("com.h2database" % "h2" % "1.4.196",
    "com.typesafe.slick" %% "slick" % "3.3.3",
    "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
    "joda-time" % "joda-time" % "2.10.10")  // https://mvnrepository.com/artifact/joda-time/joda-time

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.abhinav.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.abhinav.binders._"
