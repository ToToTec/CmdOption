import java.util.LinkedHashMap;

import de.tobiasroeser.jackage.build.*;
import de.tobiasroeser.jackage.build.utils.*;

public class Jackbuild implements Build {

	public void configure(final Builder builder) {
		MavenJarDependency.setDefaultMvnRepo(".m2repo");

		final String name = "de.tobiasroeser.cmdoption";
		final String version = "0.0.3";
		final String jar = "target/" + name + "-" + version + ".jar";


		// Java build

		builder.goal("all", jar).help("Build project and compile jar.");

		builder.goal("clean", "", new Goal() {
			public void execute(GoalContext context) {
				FileUtils.delete("target");
			}
		});

		builder.usePlugin("de.tobiasroeser.jackage.build.plugins.Java", "java");

		builder.fileGoal(jar, "java:compile", new Goal() {
			public void execute(GoalContext context) {
				FileUtils.jar(jar, "target/classes");
			}
		});

		// Tests

		builder.usePlugin("de.tobiasroeser.jackage.build.plugins.Java", "javaTest");
		builder.setProp("javaTest:sourceDir", "src/test/java");
		builder.setProp("javaTest:outputDir", "target/test-classes");
		builder.goal("javaTest:compile", jar);

		final MavenJarDependency testNg = MavenJarDependency.mvn("org.testng:testng:5.11:jdk15");
		testNg.addFileGoal(builder).reversePrereqs("javaTest:compile");

		final String testjar = "target/tests.jar";
		builder.fileGoal(testjar, "javaTest:compile", new Goal() {
			public void execute(GoalContext context) {
				FileUtils.jar(testjar, "target/test-classes");
			}
		});

		builder.goal("test", testjar, new Goal() {
			public void execute(GoalContext context) {
				final String cp = FileUtils.formatClasspath(jar, testNg.resolve());
				JavaUtils.exec("java", "-cp", cp, "org.testng.TestNG", "-d", "target/test-output", "-testjar", testjar);
			}
		});

		builder.goal("deploy-to-mvn", jar, new Goal() {
			public void execute(GoalContext context) {
				final String repo = builder.getProp("DEPLOY_MVN_REPO");
				if(repo == null) {
					context.error("Please give remote maven repo url with -D DEPLOY_MVN_REPO");
				}

				JavaUtils.exec("mvn", "deploy:deploy-file", "-Durl", repo, "-DrepositoryId", "repo", "-Dfile=" + jar, 
"-DgroupId=de.tobiasoeser", "-DartifactId=cmdoption", "-Dversion=" + version, "-DgeneratePom=true", "-DrepositoryLayout=default");
			}
		});

		builder.fileGoal("target/bnd.jar", "java:compile", new Goal() {
			public void execute(GoalContext context) {
				JarDependency bnd = new UrlJarDependency("http://www.aqute.biz/repo/biz/aQute/bnd/0.0.384/bnd-0.0.384.jar");
				JavaUtils.exec("java", "-jar", bnd.resolve(), "build", 
				"-output", context.getGoalFileName(), "-classpath", "target/classes", 
				"osgi.bnd");
			}
		}).help("Test: create a bnd-jar");

	}
}
