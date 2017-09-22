import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable.Seq

// Extends polyglot API for convenience
implicit class ImplDependency(d: Dependency) {

  def copy(gav: Gav = d.gav,
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

Model(
  gav = "de.tototec" % "de.tototec.cmdoption" % "0.5-SNAPSHOT",
  modelVersion = "4.0.0",
  packaging = "jar",
  name = "CmdOption",
  description = "CmdOption is a simple annotation-driven command line parser toolkit for Java 5 applications that is configured through annotations.",
  url = "https://github.com/ToToTec/CmdOption",
  licenses = Seq(
    License(
      name = "The Apache Software License, Version 2.0",
      url = "http://www.apache.org/licenses/LICENSE-2.0.txt",
      distribution = "repo"
    )
  ),
  scm = Scm(
    url = "https://github.com/ToToTec/CmdOption.git",
    connection = "https://github.com/ToToTec/CmdOption.git"
  ),
  developers = Seq(
    Developer(
      id = "TobiasRoeser",
      name = "Tobias Roeser",
      email = "tobias.roeser@tototec.de"
    )
  ),
  properties = Map(
    "project.build.sourceEncoding" -> "UTF-8",
    "maven.compiler.source" -> "1.5",
    "maven.compiler.target" -> "1.5",
    "maven.compiler.testSource" -> "1.8",
    "maven.compiler.testTarget" -> "1.8",
    "maven.compiler.showDeprecation" -> "true",
    "maven.compiler.showWarnings" -> "true"
  ),
  // non-transitive dependencies
  dependencies = Seq[Dependency](
    // We detect SLF4J at runtime, but it is not required
    Dependency(
      gav = "org.slf4j" % "slf4j-api" % "1.7.25",
      scope = "provided",
      optional = true
    ),
    "org.testng" % "testng" % "6.11" % "test",
    "com.beust" % "jcommander" % "1.72" % "test",
    "de.tototec" % "de.tobiasroeser.lambdatest" % "0.2.4" % "test"
  ).map(_.pure),
  build = Build(
    resources = Seq(
      Resource(
        directory = "src/main/resources"
      ),
      Resource(
        directory = ".",
        includes = Seq("LICENSE.txt")
      )
    ),
    plugins = Seq(
      Plugin(
        gav = "org.apache.maven.plugins" % "maven-surefire-plugin" % "2.20.1",
        configuration = Config(
          // Avoid string differences in tests because of locale-dependent translations
          argLine = "-Duser.country=EN -Duser.language=en -Duser.variant="
        )
      ),
      Plugin(
        gav = "org.apache.maven.plugins" % "maven-antrun-plugin" % "1.8",
        executions = Seq(
          Execution(
            id = "extract-messages",
            phase = "process-sources",
            goals = Seq("run"),
            configuration = Config(
              target = Config(
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
              ) //< target
            )
          ),
          // maybe, we should not run this always
          Execution(
            id = "merge-messages",
            phase = "generate-resources",
            goals = Seq("run"),
            configuration = Config(
              target = Config(
                // run msgmerge on the translation files to update them
                apply = new Config(Seq(
                  "@executable" -> Some("msgmerge"),
                  "@verbose" -> Some("true"),
                  "@failOnError" -> Some("true"),
                  // one invocation for each translation files
                  "@parallel" -> Some("false"),
                  // ensure, we pass the files relative to their fileset roots
                  "arg" -> Some(Config(`@value` = "--update")),
                  "srcfile" -> None,
                  "fileset" -> Some(Config(`@dir` = "${project.basedir}/src/main/po", `@includes` = "*.po")),
                  "arg" -> Some(Config(`@value` = "${project.basedir}/target/po/messages.pot"))
                ))
              ) //< target
            )
          ),
          Execution(
            id = "generate-properties",
            phase = "generate-resources",
            goals = Seq("run"),
            configuration = Config(
              target = Config(
                mkdir = Config(`@dir` = "${project.basedir}/target/classes/de/tototec/cmdoption"),
                // run msgmerge on the translation files to generate property files
                apply = new Config(Seq(
                  "@executable" -> Some("msgmerge"),
                  "@verbose" -> Some("true"),
                  "@failOnError" -> Some("true"),
                  // one invocation for each translation files
                  "@parallel" -> Some("false"),
                  "@dest" -> Some("${project.basedir}/target/classes/de/tototec/cmdoption"),
                  // ensure, we pass the files relative to their fileset roots
                  "arg" -> Some(Config(`@value` = "--output-file")),
                  // marker for the target file position
                  "targetfile" -> Some(Config(`@suffix` = "")),
                  "arg" -> Some(Config(`@value` = "--properties-output")),
                  // marker for the source file position
                  "srcfile" -> None,
                  // source files  
                  "fileset" -> Some(Config(`@dir` = "${project.basedir}/src/main/po", `@includes` = "*.po")),
                  "arg" -> Some(Config(`@value` = "${project.basedir}/target/po/messages.pot")),
                  "mapper" -> Some(Config(
                    `@type` = "glob",
                    `@from` = "*.po",
                    `@to` = "Messages_*.properties"
                  ))
                ))
              ) //< target
            )
          )
        )
      )
    )
  ),
  profiles = Seq(
    Profile(
      id = "release",
      build = BuildBase(
        plugins = Seq(
          // build source jar
          Plugin(
            gav = "org.apache.maven.plugins" % "maven-source-plugin" % "3.0.1",
            executions = Seq(
              Execution(
                id = "attach-source",
                goals = Seq("jar-no-fork")
              )
            )
          ),
          // build javadoc jar
          Plugin(
            gav = "org.apache.maven.plugins" % "maven-javadoc-plugin" % "3.0.0-M1",
            executions = Seq(
              Execution(
                id = "attach-javadoc",
                goals = Seq("jar")
              )
            )
          )
        // deployment to OSSRH Nexus
        //          Plugin(
        //            gav = "org.sonatype.plugins" % "nexus-staging-maven-plugin" % "1.6.8",
        //            // this enables automatic replacement of default deploy plugin
        //            extensions = true,
        //            configuration = Config(
        //              serverId = "ossrh",
        //              nexusUrl = "https://oss.sonatype.org/"
        //            )
        //          )
        )
      )
    ),
    Profile(
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
  )
)
