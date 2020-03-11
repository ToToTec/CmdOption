import mill._
import mill.define.Target
import mill.scalalib._
import mill.scalalib.publish._

val baseDir = build.millSourcePath

object Deps {
  val junit = ivy"junit:junit:4.12"
  val junitInterface = ivy"com.novocode:junit-interface:0.11"
  val lambdatest = ivy"de.tototec:de.tobiasroeser.lambdatest:0.3.0"
  val slf4j = ivy"org.slf4j:slf4j-api:1.7.25"
}

object cmdoption extends MavenModule with PublishModule {
  override def millSourcePath = super.millSourcePath / os.up / "de.tototec.cmdoption"
  override def artifactName = "de.tototec.cmdoption"
  override def publishVersion = "0.6.1-SNAPSHOT"
  def pomSettings = T {
    PomSettings(
      description = "CmdOption is a simple annotation-driven command line parser toolkit for Java 5 applications that is configured through annotations",
      organization = "de.tototec",
      url = "https://github.com/ToToTec/CmdOption",
      licenses = Seq(License.`Apache-2.0`),
      versionControl = VersionControl.github("ToToTec", "CmdOption"),
      developers = Seq(Developer("TobiasRoeser", "Tobias Roeser", "https.//github.com/lefou"))
    )
  }
  def javacOptions = Seq("-source", "1.5", "-target", "1.5", "-encoding", "UTF-8")
  def compileIvyDeps = Agg(Deps.slf4j.optional(true))
  def sources = T.sources(millSourcePath / "src" / "main" / "java")
  def poSource = T.source(millSourcePath / "src" / "main" / "po")
  def msgCatalog = T {
    val dest = T.ctx().dest
    val srcDir = sources().head.path
    val src = srcDir.relativeTo(baseDir).toString()
    val res = os.proc(
      "xgettext",
      "-ktr", "-kmarktr", "-kpreparetr",
      "--sort-by-file",
      "--directory", src,
      "--output-dir", dest.toIO.getPath(),
      "--output", "messages.pot",
      os.walk(srcDir).filter(_.ext == "java").map(_.relativeTo(srcDir))
    ).call(cwd = baseDir)
    println(res.out.text())
    Console.err.println(res.err.text())
    PathRef(dest / "messages.pot")
  }
   def updateTranslations() = T.command {
    val dest = T.ctx().dest
    val poFiles: Seq[os.Path] = os.list(poSource().path).filter(_.ext == "po")
    poFiles.map{ poFile =>
      println(s"File: ${poFile.relativeTo(baseDir)}")
      val res = os.proc(
        "msgmerge", "-v", "--backup=none", "--update",
        poFile.toIO.getPath(),
        msgCatalog().path.toIO.getPath()
      ).call(cwd = baseDir)
      println(res.out.text())
      Console.err.println(res.err.text())
    }
  }
  override def resources = T.sources{
    super.resources() ++ Seq(generateProperties())
  }
  def generateProperties: Target[PathRef] = T {
    val dest = T.ctx().dest
    val poFiles: Seq[os.Path] = os.list(poSource().path).filter(_.ext == "po")
    poFiles.map { poFile =>
      val lang = poFile.baseName
      val target = dest / "de" / "tototec" / "cmdoption" / s"Messages_${lang}.properties"
      println(s"File: ${target.relativeTo(baseDir)}")
      os.makeDir.all(target / os.up)
      val res = os.proc(
        "msgmerge", "-v",
        "--output-file",
        target,
        "--properties-output",
        poFile.toIO.getPath(),
        msgCatalog().path.toIO.getPath()
      ).call(cwd = baseDir)
      println(res.out.text())
      Console.err.println(res.err.text())
    }
    PathRef(dest)
  }

  object test extends Tests {
    def testFrameworks = Seq("com.novocode.junit.JUnitFramework")
    def ivyDeps = Agg(
      Deps.junitInterface,
      Deps.lambdatest
    )
  def javacOptions = Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8")
  }

}
