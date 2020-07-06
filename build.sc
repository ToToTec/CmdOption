import cmdoption.{generateProperties, generatePropertiesPackage, millSourcePath, sources}
import mill._
import mill.define.{Source, Target}
import mill.scalalib._
import mill.scalalib.publish._

import $ivy.`de.tototec::de.tobiasroeser.mill.osgi:0.2.0`
import de.tobiasroeser.mill.osgi._


val baseDir = build.millSourcePath
val cmdOptionVersion = "0.7.0"

object Deps {
  val junit = ivy"junit:junit:4.13"
  val junitInterface = ivy"com.novocode:junit-interface:0.11"
  val lambdatest = ivy"de.tototec:de.tobiasroeser.lambdatest:0.7.0"
  val slf4j = ivy"org.slf4j:slf4j-api:1.7.25"
}

trait GettextJavaModule extends JavaModule {

  def poSource: Source = T.source(millSourcePath / "src" / "main" / "po")

  /**
   * Generates a message catalog (messages.pot) from sources.
   */
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

  /**
   * Updates all found translation files (`*.po`) with current extracted message catalog.
   * @param backup If `true` creates a backup file for each updates translation file.
   */
  def updateTranslations(backup: Boolean = false) = T.command {
    val dest = T.ctx().dest
    val poFiles: Seq[os.Path] = os.list(poSource().path).filter(_.ext == "po")
    poFiles.map{ poFile =>
      println(s"Updating ${poFile.relativeTo(baseDir)}")
      val res = os.proc(
        "msgmerge", "-v",
        if(backup) Seq() else Seq("--backup=none"),
        "--update",
        poFile.toIO.getPath(),
        msgCatalog().path.toIO.getPath()
      ).call(cwd = baseDir)
    }
  }

  /**
   * The Java package in which the generated properties files will end up.
   */
  def generatePropertiesPackage: Target[String] = ""


  /**
   * Generate properties files for each translation file (`*.po`).
   */
  def generateProperties: Target[PathRef] = T {
    val dest = T.ctx().dest
    val poFiles: Seq[os.Path] = os.list(poSource().path).filter(_.ext == "po")
    poFiles.map { poFile =>
      val lang = poFile.baseName
      // hardcoded location
      val target = dest / os.RelPath(generatePropertiesPackage()) / s"Messages_${lang}.properties"
      println(s"Processing ${poFile.relativeTo(baseDir)} to ${target.relativeTo(baseDir)}")
      os.makeDir.all(target / os.up)
      val res = os.proc(
        "msgmerge", "-v",
        "--output-file",
        target,
        "--properties-output",
        poFile.toIO.getPath(),
        msgCatalog().path.toIO.getPath()
      ).call(cwd = baseDir)
    }
    println(s"You can update existing message catalogs with: mill ${updateTranslations()}")
    PathRef(dest)
  }

  /**
   * Add generated properties files to resources.
   */
  override def resources = T.sources{
    super.resources() ++ Seq(generateProperties())
  }

}


object cmdoption extends MavenModule with GettextJavaModule with OsgiBundleModule with PublishModule {
  override def millSourcePath = super.millSourcePath / os.up / "de.tototec.cmdoption"
  val namespace = "de.tototec.cmdoption"
  override def artifactName = namespace
  override def publishVersion = cmdOptionVersion
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
  override def javacOptions = Seq("-source", "1.6", "-target", "1.6", "-encoding", "UTF-8")
  override def compileIvyDeps = Agg(Deps.slf4j.optional(true))
  override def sources = T.sources(millSourcePath / "src" / "main" / "java")
  override def generatePropertiesPackage: Target[String] = "de.tototec.cmdoption"

  override def osgiHeaders = T { super.osgiHeaders().copy(
    `Bundle-SymbolicName` = namespace,
    `Bundle-Name` = Some(s"CmdOption ${publishVersion()}"),
    `Export-Package` = Seq(
      namespace,
      s"$namespace.handler"
    ),
    `Import-Package` = Seq(
        "org.slf4j.*;resolution:=optional",
        "*"
    )
  )}


  object test extends Tests {
    def testFrameworks = Seq("com.novocode.junit.JUnitFramework")
    override def ivyDeps = Agg(
      Deps.junit,
      Deps.junitInterface,
      Deps.lambdatest
    )
  override def javacOptions = Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8")
  }

}
