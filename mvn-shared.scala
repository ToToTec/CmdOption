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

object Gettext {

  def extractMessagesTarget: Config = Config(
    mkdir = Config(`@dir` = "${project.basedir}/target/po"),
    // run xgettext on the source files
    apply = Config(
      `@verbose` = "true",
      `@failOnError` = "true",
      `@executable` = "xgettext",
      // only one invocation with all files
      `@parallel` = "true",
      // ensure, we pass the files relative to their fileset roots
      `@relative` = "true",
      arg = Config(`@value` = "-ktr"),
      arg = Config(`@value` = "-kmarktr"),
      arg = Config(`@value` = "-kpreparetr"),
      arg = Config(`@value` = "--sort-by-file"),
      arg = Config(`@value` = "--directory"),
      arg = Config(`@value` = "src/main/java"),
      arg = Config(`@value` = "--output-dir"),
      arg = Config(`@value` = "${project.basedir}/target/po"),
      arg = Config(`@value` = "--output"),
      arg = Config(`@value` = "messages.pot"),
      // the source files to scan
      fileset = Config(`@dir` = "${project.basedir}/src/main/java")
    )
  )

  def mergeMessagesTarget: Config = Config(
    // run msgmerge on the translation files to update them
    apply = Config(
      `@executable` = "msgmerge",
      `@verbose` = "true",
      `@failOnError` = "true",
      // one invocation for each translation files
      `@parallel` = "false",
      arg = Config(`@value` = "--backup=none"),
      // ensure, we pass the files relative to their fileset roots
      arg = Config(`@value` = "--update"),
      srcfile = Config(`@suffix` = ""),
      fileset = Config(`@dir` = "${project.basedir}/src/main/po", `@includes` = "*.po"),
      arg = Config(`@value` = "${project.basedir}/target/po/messages.pot")
    )
  )

  def generatePropertiesTarget: Config = Config(
    mkdir = Config(`@dir` = "${project.basedir}/target/classes/de/tototec/cmdoption"),
    // run msgmerge on the translation files to generate property files
    apply = Config(
      `@executable` = "msgmerge",
      `@verbose` = "true",
      `@failOnError` = "true",
      // one invocation for each translation files
      `@parallel` = "false",
      `@dest` = "${project.basedir}/target/classes/de/tototec/cmdoption",
      // ensure, we pass the files relative to their fileset roots
      arg = Config(`@value` = "--output-file"),
      // marker for the target file position
      targetfile = Config(`@suffix` = ""),
      arg = Config(`@value` = "--properties-output"),
      // marker for the source file position
      srcfile = Config(`@suffix` = ""),
      // source files  
      fileset = Config(`@dir` = "${project.basedir}/src/main/po", `@includes` = "*.po"),
      arg = Config(`@value` = "${project.basedir}/target/po/messages.pot"),
      mapper = Config(
        `@type` = "glob",
        `@from` = "*.po",
        `@to` = "Messages_*.properties"
      )
    )
  )
}