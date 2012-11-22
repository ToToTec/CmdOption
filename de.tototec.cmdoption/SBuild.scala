import de.tototec.sbuild._
import de.tototec.sbuild.TargetRefs._
import de.tototec.sbuild.ant._
import de.tototec.sbuild.ant.tasks._

@version("0.1.0.9002")
@classpath("http://repo1.maven.org/maven2/org/apache/ant/ant/1.8.3/ant-1.8.3.jar")
class SBuild(implicit project: Project) {

  // some helper
  implicit def stringToPath(string: String) = Path(string)

  val version = "SVN"
  val jar = "target/de.tototec.cmdoption-" + version + ".jar"

  SchemeHandler("mvn", new MvnSchemeHandler())
  
  val testCp = "mvn:org.testng:testng:6.1"

  ExportDependencies("eclipse.classpath", testCp)

  Target("phony:all") dependsOn jar

  Target("phony:clean") exec {
    AntDelete(dir = "target")
  }

  val javaFiles = (Path("src/main/java/de/tototec/cmdoption").listFiles ++
    Path("src/main/java/de/tototec/cmdoption/handler").listFiles).
    filter(_.getName.endsWith(".java"))

  Target("phony:compile") exec { ctx: TargetContext =>
    IfNotUpToDate("src/main/java", "target", ctx) {
      AntMkdir(dir = "target/classes")
      AntJavac(
        source = "1.5",
        target = "1.5",
        encoding = "UTF-8",
        destDir = "target/classes",
        srcDir = AntPath("src/main/java"),
        debug = true,
        fork = true,
        includeAntRuntime = false
      )
    }
  }

  val msgCatalog = Path("target/po/messages.pot")

  Target("phony:xgettext") exec { ctx: TargetContext =>
    IfNotUpToDate("src/main/java", "target", ctx) {
      AntMkdir(dir = msgCatalog.getParentFile)

      import java.io.File
      val srcDirUri = Path("src/main/java").toURI

      AntExec(
        failOnError = true,
        executable = "xgettext",
        args = Array[String](
          "-ktr", "-kmarktr",
          "--directory", new File(srcDirUri).getPath,
          "--output-dir", msgCatalog.getParent,
          "--output", msgCatalog.getName) ++ javaFiles.map(file => srcDirUri.relativize(file.toURI).getPath)
      )
    }
  }

  Target(msgCatalog) dependsOn "xgettext"

  val poFiles = Path("src/main/po").listFiles.filter(f => f.getName.endsWith(".po"))
  val propFileTargets = poFiles.map { poFile =>
    val propFile = Path("target/classes/de/tototec/cmdoption", "Message_" + """\.po$""".r.replaceFirstIn(poFile.getName, ".properties"))
    Target(propFile) dependsOn (msgCatalog ~ poFile) exec {
      AntMkdir(dir = propFile.getParentFile)
      AntExec(
        failOnError = true,
        executable = "msgmerge",
        args = Array("--output-file", propFile.getPath, "--properties-output", poFile.getPath, msgCatalog.getPath)
      )
    }
  }

  val jarTarget = Target(jar) dependsOn ("compile") exec {
    AntJar(baseDir = "target/classes", destFile = jar)
  }

  propFileTargets.foreach { t => jarTarget dependsOn t }

}
