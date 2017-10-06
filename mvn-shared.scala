object CmdOption {

  val version = "0.5-SNAPSHOT"

}

val genPomXmlProfile = Profile(
      id = "gen-pom-xml",
      build = BuildBase(
        plugins = Seq(
          // Generate pom.xml from pom.scala
          Plugin(
            gav = "io.takari.polyglot" % "polyglot-translate-plugin" % "0.2.1",
            executions = Seq(
              Execution(
                id = "pom-scala-to-pom-xml",
                phase = "initialize",
                goals = Seq("translate-project"),
                configuration = Config(
                  input = "pom.scala",
                  output = "pom.xml"
                )
              )
            )
          ),
          // Clean generated pom.xml
          Plugin(
            gav = "org.apache.maven.plugins" % "maven-clean-plugin" % "3.0.0",
            configuration = Config(
              filesets = Config(
                fileset = Config(
                  directory = "${basedir}",
                  includes = Config(
                    include = "pom.xml"
                  )
                )
              )
            )
          )
        )
      )
    )