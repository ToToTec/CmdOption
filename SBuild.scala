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

  val binDistFiles = Seq(
      "de.tototec.cmdoption/target/de.tototec.cmdoption-" + CmdOption.version + ".jar",
      "de.tototec.cmdoption/target/de.tototec.cmdoption-" + CmdOption.version + "-sources.jar",
      "de.tototec.cmdoption/target/de.tototec.cmdoption-" + CmdOption.version + "-javadoc.jar",
      srcDistZip,
      "LICENSE.txt"
  )

  Target("phony:clean") dependsOn "de.tototec.cmdoption::clean" exec {
    AntDelete(dir = Path("target"))
  }

  Target("phony:all") dependsOn "de.tototec.cmdoption::all" ~ srcDistZip ~ binDistZip ~ "prepareMvnStaging"

  // depend on all source files
  Target(srcDistZip) exec { ctx: TargetContext =>
    AntMkdir(dir = Path("target"))
    val targetDir = "target/" + srcDist
    AntDelete(dir = Path(targetDir))
    AntExec(executable = "git", args = Array("archive", "--format", "zip", "--output", ctx.targetFile.get.getPath(), "master"))
  }

  Target(binDistZip) dependsOn binDistFiles.map{ n => TargetRef(n) } exec { ctx: TargetContext =>
    AntZip(destFile = ctx.targetFile.get, fileSets = binDistFiles.map { file =>
      new org.apache.tools.ant.types.ZipFileSet(AntFileSet(file = Path(file))) {
        setPrefix(binDist)
      }}
    )
  }

  Target("phony:prepareMvnStaging") dependsOn binDistFiles.filter(_.endsWith(".jar")).map{ f => TargetRef(f) } exec { ctx: TargetContext =>
    AntMkdir(dir = Path("target/mvn"))

    val pom = """<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>de.tototec</groupId>
  <artifactId>de.tototec.cmdoption</artifactId>
  <packaging>jar</packaging>
  <version>""" + CmdOption.version + """</version>
  <name>CmdOption</name>
  <description>CmdOption is a simple annotation-driven command line parser toolkit for Java 5 applications that is configured through annotations.</description>
  <url>http://cmdoption.tototec.de</url>
  <licenses>
    <license>
      <name>The Apache Software License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>http://cmdoption.tototec.de/svn/cmdoption</url>
    <connection>http://cmdoption.tototec.de/svn/cmdoption</connection>
  </scm>
  <developers>
    <developer>
      <id>TobiasRoeser</id>
      <name>Tobias Roeser</name>
      <email>tobias.roeser@tototec.de</email>
    </developer>
  </developers>
</project>"""

    AntEcho(message = pom, file = Path("target/mvn/pom.xml"))

    ctx.fileDependencies.foreach { file =>
      AntCopy(file = file, toDir = Path("target/mvn"))
    }

    val script = """#!/bin/sh

echo "Please edit settings.xml with propper connection details."
read

echo "Uploading jar"
mvn -s ./settings.xml gpg:sign-and-deploy-file -DpomFile=pom.xml -Dfile=de.tototec.cmdoption-""" + CmdOption.version + """.jar -Durl=http://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging

echo "Uploading sources"
mvn -s ./settings.xml gpg:sign-and-deploy-file -DpomFile=pom.xml -Dfile=de.tototec.cmdoption-""" + CmdOption.version + """-sources.jar -Dclassifier=sources -Durl=http://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging

echo "Uploading javadoc"
mvn -s ./settings.xml gpg:sign-and-deploy-file -DpomFile=pom.xml -Dfile=de.tototec.cmdoption-""" + CmdOption.version + """-javadoc.jar -Dclassifier=javadoc -Durl=http://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging
"""

    AntEcho(message = script, file = Path("target/mvn/script.sh"))

    val settings = """<settings>
  <servers>
    <server>
      <id>sonatype-nexus-staging</id>
      <username>your-username</username>
      <password>your-password</password>
    </server>
  </servers>
</settings>
"""

    AntEcho(message = settings, file = Path("target/mvn/settings.xml"))

  }

}
