ThisBuild / scalaVersion := "3.3.3"
ThisBuild / version      := "0.1.0"

name := "ejercicio-ondas-scalajs"

enablePlugins(ScalaJSPlugin)

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.0"

scalaJSUseMainModuleInitializer := true