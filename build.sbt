val appName = "bootstrap-play-25"

lazy val library = Project(appName, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    majorVersion := 3,
    makePublicallyAvailableOnBintray := true
  )
  .settings(
    scalaVersion := "2.11.12",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    javaOptions in Test ++= Seq(
      "-Dconfig.resource=application.test.conf"
    ),
    fork in Test := true,
    scalacOptions ++= Seq("-deprecation"),
    resolvers := Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.bintrayRepo("hmrc", "snapshots"),
      Resolver.bintrayRepo("hmrc", "release-candidates"),
      Resolver.typesafeRepo("releases"),
      Resolver.jcenterRepo
    )
  )
