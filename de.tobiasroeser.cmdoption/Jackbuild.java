import java.util.LinkedHashMap;

import de.tobiasroeser.jackage.build.*;
import de.tobiasroeser.jackage.build.utils.*;

public class Jackbuild implements Build {

	public void configure(Builder builder) {
		MavenJarDependency.setDefaultMvnRepo(".m2repo");

		final String name = "de.tobiasroeser.cmdoption";
		final String version = "0.0.2";
		final String jar = "target/" + name + "-" + version + ".jar";


		// Java build

		builder.goalHelp("all", "Build project and compile jar.");
		builder.goal("all", jar);

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
		testNg.addAsFileGoal(builder, "javaTest:compile");

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

	}
}
