import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable.Seq

#include mvn-shared.scala

Model(
  gav = CmdOption.groupId % "de.tototec.cmdoption-reactor" % CmdOption.version,
  modelVersion = "4.0.0",
  packaging = "pom",
  modules = Seq(
    "de.tototec.cmdoption"
  ),
  build = Build(
    plugins = Seq(
      Plugin(
        gav = Plugins.install,
        configuration = Config(
          skip = true
        )
      ),
      Plugin(
        gav = Plugins.deploy,
        configuration = Config(
          skip = true
        )
      )
    )
  ),
  profiles = Seq(
    genPomXmlProfile
  )
)
