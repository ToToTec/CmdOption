import de.tototec.sbuild._
import de.tototec.sbuild.TargetRefs._
import de.tototec.sbuild.ant._
import de.tototec.sbuild.ant.tasks._

@version("0.2.0")
@include("../CmdOption.scala")
@classpath("http://repo1.maven.org/maven2/org/apache/ant/ant/1.8.4/ant-1.8.4.jar")
class SBuild(implicit _project: Project) {

  val version = CmdOption.version

  val jar = "target/de.tototec.cmdoption-" + version + ".jar"
  val sourcesJar = jar.substring(0, jar.length - 4) + "-sources.jar"
  val javadocJar = jar.substring(0, jar.length - 4) + "-javadoc.jar"

  SchemeHandler("mvn", new MvnSchemeHandler())

  val testCp = "mvn:org.testng:testng:6.4" ~
    "mvn:com.beust:jcommander:1.30"

  ExportDependencies("eclipse.classpath", testCp)

  val javaFiles = (Path("src/main/java/de/tototec/cmdoption").listFiles ++
    Path("src/main/java/de/tototec/cmdoption/handler").listFiles).
    filter(_.getName.endsWith(".java"))

  val poFiles = Path("src/main/po").listFiles.filter(f => f.getName.endsWith(".po"))

  Target("phony:all") dependsOn jar ~  sourcesJar ~ "test"

  Target("phony:clean") exec {
    AntDelete(dir = Path("target"))
  }

  def compileJava(sources: java.io.File, destDir: java.io.File, ctx: TargetContext) = {
    IfNotUpToDate(sources, Path("target"), ctx) {
      AntMkdir(dir = destDir)
      AntJavac(source = "1.5", target = "1.5", encoding = "UTF-8",
        debug = true, fork = true, includeAntRuntime = false,
        classpath = AntPath(locations = ctx.fileDependencies),
        srcDir = AntPath(sources),
        destDir = destDir
      )
    }
  }

  Target("phony:compile") exec { ctx: TargetContext =>
    compileJava(sources = Path("src/main/java"), destDir = Path("target/classes"), ctx = ctx)
  }

  Target("phony:compileTest") dependsOn testCp ~ jar exec { ctx: TargetContext =>
    compileJava(sources = Path("src/test/java"), destDir = Path("target/test-classes"), ctx = ctx)
  }

  Target("phony:test") dependsOn "compileTest" ~ testCp ~ jar exec { ctx: TargetContext =>
    val tests = Seq(
      "de.tototec.cmdoption.DelegateTest",
      "de.tototec.cmdoption.ExampleSemVerTest",
      "de.tototec.cmdoption.ParserTest",
      "de.tototec.cmdoption.handler.UrlHandlerTest"
    )

    AntJava(failOnError = true, classpath = AntPath(locations = ctx.fileDependencies ++ Seq(Path("target/test-classes"))),
      className = "org.testng.TestNG", arguments = Seq("-testclass", tests.mkString(","), "-d", Path("target/test-output").getPath))
  }

  val msgCatalog = Path("target/po/messages.pot")

  Target(msgCatalog) dependsOn javaFiles.map { TargetRef(_) }.toSeq exec { ctx: TargetContext =>
    AntMkdir(dir = ctx.targetFile.get.getParentFile)

    import java.io.File
    val srcDirUri = Path("src/main/java").toURI

    AntExec(
      failOnError = true,
      executable = "xgettext",
      args = Array[String](
        "-ktr", "-kmarktr",
        "--directory", new File(srcDirUri).getPath,
        "--output-dir", ctx.targetFile.get.getParent,
        "--output", ctx.targetFile.get.getName) ++ ctx.fileDependencies.map(file => srcDirUri.relativize(file.toURI).getPath)
    )
  }

  val propFileTargets = poFiles.map { poFile =>
    val propFile = Path("target/classes/de/tototec/cmdoption", "Messages_" + """\.po$""".r.replaceFirstIn(poFile.getName, ".properties"))
    Target(propFile) dependsOn (msgCatalog ~ poFile) exec {
      AntMkdir(dir = propFile.getParentFile)
      AntExec(
        failOnError = true,
        executable = "msgmerge",
        args = Array("--output-file", propFile.getPath, "--properties-output", poFile.getPath, msgCatalog.getPath)
      )
    }
  }

  Target("phony:msgmerge") dependsOn msgCatalog exec {
    poFiles.foreach { poFile =>
      AntExec(failOnError = true, executable = "msgmerge",
        args = Array("--update", poFile.getPath, msgCatalog.getPath))
    }
  } help "Updates translation files (.po) with newest messages."

  val jarTarget = Target(jar) dependsOn ("compile") exec {
    AntJar(baseDir = Path("target/classes"), destFile = Path(jar), fileSet = AntFileSet(file = Path("LICENSE.txt")))
  }

  propFileTargets.foreach { t => jarTarget dependsOn t }

  Target("phony:installToMvn") dependsOn jar exec {
    AntExec(
      executable = "mvn",
      args = Array("install:install-file", "-DgroupId=de.tototec", "-DartifactId=de.tototec.cmdoption", "-Dversion=" + version, "-Dfile=" + jar, "-DgeneratePom=true", "-Dpackaging=jar"))
  } help "Install jar into Maven repository."

  Target(sourcesJar) exec { ctx: TargetContext =>
    IfNotUpToDate(Path("src/main/"), Path("target"), ctx) {
      AntJar(destFile = ctx.targetFile.get, fileSets = Seq(
        AntFileSet(dir = Path("src/main/java")),
        AntFileSet(dir = Path("src/main/po")),
        AntFileSet(file = Path("LICENSE.txt"))
      ))
    }
  }

  Target(javadocJar) exec { ctx: TargetContext =>
    IfNotUpToDate(Path("src/main/java"), Path("target"), ctx) {

      val docDir = Path("target/javadoc")
      AntMkdir(dir = docDir)

      new org.apache.tools.ant.taskdefs.Javadoc() {
        setProject(AntProject())
        setSourcepath(AntPath(location = Path("src/main/java")))
        setDestdir(docDir)
      }.execute

      AntJar(destFile = ctx.targetFile.get, baseDir = docDir)
    }
  }

}
