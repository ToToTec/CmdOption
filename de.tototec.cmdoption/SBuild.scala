import de.tototec.sbuild._
import de.tototec.sbuild.TargetRefs._
import de.tototec.sbuild.ant._
import de.tototec.sbuild.ant.tasks._

@version("0.4.0")
@include("../CmdOption.scala")
@classpath("mvn:org.apache.ant:ant:1.8.4")
class SBuild(implicit _project: Project) {

  val version = CmdOption.version

  val jar = s"target/de.tototec.cmdoption-$version.jar"
  val sourcesJar = jar.substring(0, jar.length - 4) + "-sources.jar"
  val javadocJar = jar.substring(0, jar.length - 4) + "-javadoc.jar"

  val compileCp =
    "mvn:org.slf4j:slf4j-api:1.7.5"

  val testCp =
    "mvn:org.testng:testng:6.8.8" ~
      "mvn:com.beust:jcommander:1.30" ~ // transitive required by testng
      "mvn:org.scalatest:scalatest_2.10:2.2.2" ~
      "mvn:org.scala-lang:scala-library:2.10.2" ~
      "mvn:org.scala-lang:scala-actors:2.10.2" ~
      "mvn:org.slf4j:slf4j-api:1.7.5" ~
      "mvn:ch.qos.logback:logback-core:1.1.0" ~
      "mvn:ch.qos.logback:logback-classic:1.1.0" ~
      "mvn:de.tototec:de.tobiasroeser.lambdatest:0.0.3"

  ExportDependencies("eclipse.classpath", compileCp ~ testCp)

  val poFiles = Path("src/main/po").listFiles.filter(f => f.getName.endsWith(".po"))

  Target("phony:all") dependsOn jar ~ sourcesJar ~ "test"

  Target("phony:clean").evictCache exec {
    AntDelete(dir = Path("target"))
  }

  Target("phony:compile").cacheable dependsOn compileCp ~ "scan:src/main/java" exec {
    addons.java.Javac(
      classpath = compileCp.files,
      source = "1.5", target = "1.5", encoding = "UTF-8", debugInfo = "all",
      destDir = Path("target/classes"),
      sources = "scan:src/main/java".files
    )
  }

  Target("phony:compileTest").cacheable dependsOn testCp ~ jar ~ "scan:src/test/java" exec {
    addons.java.Javac(
      source = "1.8", target = "1.8", encoding = "UTF-8", debugInfo = "all",
      destDir = Path("target/test-classes"),
      sources = "scan:src/test/java".files,
      classpath = testCp.files ++ jar.files
    )
  }

  Target("phony:test") dependsOn "compileTest" ~ testCp ~ jar ~ "scan:src/test/resources" ~ "src/test/resources/TestNGSuite.xml" exec {
    new AntJava(
      failOnError = true, dir = Path("target"), fork = true,
      classpath = AntPath(locations = testCp.files ++ jar.files ++ Seq(Path("target/test-classes"), Path("src/test/resources"))),
      className = "org.scalatest.tools.Runner",
      arguments = Seq("-oG", "-b", "src/test/resources/TestNGSuite.xml".files.head.getPath)
    ) {
      addEnv(new org.apache.tools.ant.types.Environment.Variable() {
        setKey("LC_ALL")
        setValue("C")
      })
    }.execute()
  }

  val msgCatalog = "target/po/messages.pot"

  Target(msgCatalog) dependsOn "scan:src/main/java" exec { ctx: TargetContext =>
    AntMkdir(dir = ctx.targetFile.get.getParentFile)

    import java.io.File
    val srcDirUri = Path("src/main/java").toURI

    AntExec(
      failOnError = true,
      executable = "xgettext",
      args = Array[String](
        "-ktr", "-kmarktr", "-kpreparetr",
        // "--sort-output",
        "--sort-by-file",
        "--no-wrap",
        "--directory", new File(srcDirUri).getPath,
        "--output-dir", ctx.targetFile.get.getParent,
        "--output", ctx.targetFile.get.getName) ++ "scan:src/main/java".files.map(file => srcDirUri.relativize(file.toURI).getPath)
    )
  }

  val propFileTargets = poFiles.map { poFile =>
    val propFile = Path("target/classes/de/tototec/cmdoption", "Messages_" + """\.po$""".r.replaceFirstIn(poFile.getName, ".properties"))
    Target(propFile) dependsOn msgCatalog ~ poFile exec {
      AntMkdir(dir = propFile.getParentFile)
      AntExec(
        failOnError = true,
        executable = "msgmerge",
        args = Array("--output-file", propFile.getPath, "--properties-output", poFile.getPath(), msgCatalog.files.head.getPath())
      )
    }
  }

  Target("phony:msgmerge") dependsOn msgCatalog exec {
    poFiles.foreach { poFile =>
      AntExec(failOnError = true, executable = "msgmerge",
        args = Array("--update", poFile.getPath, msgCatalog.files.head.getPath()))
    }
  } help "Updates translation files (.po) with newest messages."

  val classes = "scan:target/classes"
  Target(classes) dependsOn "compile"
  propFileTargets.foreach { t => Target(classes) dependsOn t }

  Target(jar) dependsOn classes ~ "LICENSE.txt" ~ "ChangeLog.txt" exec {
    AntJar(baseDir = Path("target/classes"), destFile = Path(jar),
      fileSets = Seq(
        AntFileSet(file = "LICENSE.txt".files.head),
        AntFileSet(file = "ChangeLog.txt".files.head)
      )
    )
  }

  Target("phony:installToMvn") dependsOn jar exec {
    AntExec(
      executable = "mvn",
      args = Array("install:install-file", "-DgroupId=de.tototec", "-DartifactId=de.tototec.cmdoption", "-Dversion=" + version, "-Dfile=" + jar, "-DgeneratePom=true", "-Dpackaging=jar"))
  } help "Install jar into Maven repository."

  Target(sourcesJar) dependsOn "scan:src/main/java" ~ "scan:src/main/po" ~ "LICENSE.txt" ~ "ChangeLog.txt" exec { ctx: TargetContext =>
    AntJar(destFile = ctx.targetFile.get, fileSets = Seq(
      AntFileSet(dir = Path("src/main/java")),
      AntFileSet(dir = Path("src/main/po")),
      AntFileSet(file = "LICENSE.txt".files.head),
      AntFileSet(file = "ChangeLog.txt".files.head)
    ))
  }

  Target(javadocJar).cacheable dependsOn compileCp ~ "scan:src/main/java" exec { ctx: TargetContext =>
    val docDir = Path("target/javadoc")
    AntMkdir(dir = docDir)

    new org.apache.tools.ant.taskdefs.Javadoc() {
      setProject(AntProject())
      setSourcepath(AntPath(location = Path("src/main/java")))
      setClasspath(AntPath(locations = compileCp.files))
      setDestdir(docDir)
    }.execute

    AntJar(destFile = ctx.targetFile.get, baseDir = docDir)
  }

}
