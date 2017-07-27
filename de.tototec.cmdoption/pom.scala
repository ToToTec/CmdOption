import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable.Seq

// copy method on dependency
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
}

Model(
  gav = "de.tototec" % "de.tototec.cmdoption" % "0.4.3-SNAPSHOT",
  modelVersion = "4.0.0",
  packaging = "jar",
  properties = Map(
    "project.build.sourceEncoding" -> "UTF-8",
    "maven.compiler.source" -> "1.5",
    "maven.compiler.target" -> "1.5",
    "maven.compiler.testSource" -> "1.8",
    "maven.compiler.testTarget" -> "1.8",
    "maven.compiler.showDeprecation" -> "true",
    "maven.compiler.showWarnings" -> "true"
  ),
  dependencies = Seq[Dependency](
    Dependency(
      gav = "org.slf4j" % "slf4j-api" % "1.7.25",
      scope = "provided",
      optional = true
    ),
    "org.testng" % "testng" % "6.11" % "test",
    "com.beust" % "jcommander" % "1.72" % "test",
    "de.tototec" % "de.tobiasroeser.lambdatest" % "0.2.4" % "test"
  ).map(d =>
    // exclude all transitive dependencies
    d.copy(exclusions = Seq("*" % "*"))
  ),
  profiles = Seq(
    Profile(
      id = "gen-pom-xml",
      activation = Activation(),
      build = BuildBase(
        plugins = Seq(
          Plugin(
            gav = "io.takari.polyglot" % "polyglot-translate-plugin" % "0.2.0",
            // we need this dependency, because somehow without, a too old version (1.1) is used which lacks required classes
            dependencies = Seq("org.codehaus.plexus" % "plexus-utils" % "3.0.24"),
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
          )
        )
      )
    )
  )
)
