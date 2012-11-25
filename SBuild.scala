import de.tototec.sbuild._
import de.tototec.sbuild.TargetRefs._
import de.tototec.sbuild.ant._
import de.tototec.sbuild.ant.tasks._

@version("0.2.0")
@include("CmdOption.scala")
@classpath("http://repo1.maven.org/maven2/org/apache/ant/ant/1.8.4/ant-1.8.4.jar")
class SBuild(implicit project: Project) {

  Module("de.tototec.cmdoption")

  val srcDist = "cmdoption-src-" + CmdOption.version
  val binDist = "cmdoption-dist-" + CmdOption.version

  val srcDistZip = "target/" + srcDist + ".zip"
  val binDistZip = "target/" + binDist + ".zip"


  Target("phony:clean") dependsOn "de.tototec.cmdoption::clean" exec {
    AntDelete(dir = Path("target"))
  }

  Target("phony:all") dependsOn "de.tototec.cmdoption::all" ~ srcDistZip ~ binDistZip

  // depend on all source files
  Target(srcDistZip) exec { ctx: TargetContext =>
    AntMkdir(dir = Path("target"))
    val targetDir = "target/" + srcDist
    AntDelete(dir = Path(targetDir))
    AntExec(executable = "svn", args = Array("export", ".", targetDir))
    AntZip(destFile = ctx.targetFile.get, baseDir = Path("target"), includes = srcDist + "/**")
  }

  val releasedFiles = Seq(
      "de.tototec.cmdoption/target/de.tototec.cmdoption-" + CmdOption.version + ".jar",
      "de.tototec.cmdoption/target/de.tototec.cmdoption-" + CmdOption.version + "-sources.jar",
      srcDistZip
  )

  Target(binDistZip) dependsOn releasedFiles.map{ n => TargetRef(n) } exec { ctx: TargetContext =>
    AntZip(destFile = ctx.targetFile.get, fileSets = releasedFiles.map { file =>
      new org.apache.tools.ant.types.ZipFileSet(AntFileSet(file = Path(file))) {
        setPrefix(binDist)
      }}
    )
  }

}
