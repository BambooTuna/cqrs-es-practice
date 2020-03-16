import Settings._

lazy val boot = (project in file("boot"))
  .enablePlugins(JavaAppPackaging, AshScriptPlugin, DockerPlugin)
  .settings(commonSettings)
  .settings(dockerSettings)
  .settings(
    resolvers += AkkaServerSupport.resolver,
    libraryDependencies ++= Seq(
      MySQLConnectorJava.version,
      Redis.client,
      "org.simplejavamail" % "simple-java-mail" % "6.0.3",
      "org.iq80.leveldb" % "leveldb" % "0.7",
      "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
      "io.suzaku" %% "boopickle" % "1.3.1"
    ) ++ `doobie-quill`.all
  )

lazy val root =
  (project in file("."))
    .aggregate(boot)
