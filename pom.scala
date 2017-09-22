import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable.Seq

Model(
  gav = "de.tototec" % "de.tototec.cmdoption-reactor" % "1",
  modelVersion = "4.0.0",
  packaging = "pom",
  modules = Seq(
    "de.tototec.cmdoption"
  ),
  build = Build(
    plugins = Seq(
      Plugin(
        gav = "org.apache.maven.plugins" % "maven-install-plugin",
        configuration = Config(
          skip = true
        )
      ),
      Plugin(
        gav = "org.apache.maven.plugins" % "maven-deploy-plugin",
        configuration = Config(
          skip = true
        )
      )
    )
  )
)
