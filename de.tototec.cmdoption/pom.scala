import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable.Seq

#include ../mvn-shared.scala

Model(
  gav = "de.tototec" % "de.tototec.cmdoption" % CmdOption.version,
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
              target = Gettext.extractMessagesTarget
            )
          ),
          // Update/merge translations with current message catalog
          // Not bound to life-cycle
          // Please run manually with: mvn antrun:run@update-translations
          Execution(
            id = "update-translations",
            goals = Seq("run"),
            configuration = Config(
              target = new Config(
                  Gettext.extractMessagesTarget.elements ++ Gettext.mergeMessagesTarget.elements
                )
            )
          ),
          // Generate properties files for translations
          Execution(
            id = "generate-properties",
            phase = "process-resources",
            goals = Seq("run"),
            configuration = Config(
              target = Gettext.generatePropertiesTarget
            )
          )
        )
      )
    )
  ),
  profiles = Seq(
    genPomXmlProfile
  )
)
