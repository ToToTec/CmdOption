object CmdOption {
  val groupId = "de.tototec"
  val version = "0.7-SNAPSHOT"
}

object Plugins {
  val antrun = "org.apache.maven.plugins" % "maven-antrun-plugin" % "1.8"
  val bundle = "org.apache.felix" % "maven-bundle-plugin" % "3.3.0"
  val clean = "org.apache.maven.plugins" % "maven-clean-plugin" % "3.0.0"
  val dependencyCheck = "org.owasp" % "dependency-check-maven" % "3.1.1"
  val deploy = "org.apache.maven.plugins" % "maven-deploy-plugin" % "2.8.2"
  val install = "org.apache.maven.plugins" % "maven-install-plugin" % "2.5.2"
  val surefire = "org.apache.maven.plugins" % "maven-surefire-plugin" % "2.20.1"
  val translate = "io.takari.polyglot" % "polyglot-translate-plugin" % "0.2.1"
}

object Deps {
  val junit = "junit" % "junit" % "4.12"
  val lambdatest = "de.tototec" % "de.tobiasroeser.lambdatest" % "0.3.0"
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.25"
}

// Extends polyglot API for convenience
implicit class ImplDependency(d: Dependency) {

  def copy(
    gav: Gav = d.gav,
    `type`: String = d.`type`,
    classifier: String = d.classifier.orNull,
    scope: String = d.scope.orNull,
    systemPath: String = d.systemPath.orNull,
    exclusions: Seq[GroupArtifactId] = d.exclusions,
    optional: Boolean = d.optional) = {
    new Dependency(gav, `type`, Option(classifier), Option(scope), Option(systemPath), exclusions, optional)
  }

  def pure: Dependency = d.copy(exclusions = Seq("*" % "*"))
}

val genPomXmlProfile = Profile(
  id = "gen-pom-xml",
  build = BuildBase(
    plugins = Seq(
      // Generate pom.xml from pom.scala
      Plugin(
        gav = Plugins.translate,
        executions = Seq(
          Execution(
            id = "pom-scala-to-pom-xml",
            phase = "initialize",
            goals = Seq("translate-project"),
            configuration = Config(
              input = "pom.scala",
              output = "pom.xml")))),
      // Clean generated pom.xml
      Plugin(
        gav = Plugins.clean,
        configuration = Config(
          filesets = Config(
            fileset = Config(
              directory = "${basedir}",
              includes = Config(
                include = "pom.xml"))))))))

/**
 * Handle gettext related build scripts
 */
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
      fileset = Config(`@dir` = "${project.basedir}/src/main/java")))

  def mergeMessagesTarget: Config = Config(
    // run msgmerge on the translation files to update them
    apply = Config(
      `@executable` = "msgmerge",
      `@verbose` = "true",
      `@failOnError` = "true",
      // one invocation for each translation files
      `@parallel` = "false",
      arg = Config(`@value` = "-v"),
      arg = Config(`@value` = "--backup=none"),
      // ensure, we pass the files relative to their fileset roots
      arg = Config(`@value` = "--update"),
      srcfile = Config(`@suffix` = ""),
      fileset = Config(`@dir` = "${project.basedir}/src/main/po", `@includes` = "*.po"),
      arg = Config(`@value` = "${project.basedir}/target/po/messages.pot")))

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
      arg = Config(`@value` = "-v"),
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
        `@to` = "Messages_*.properties")))
}
