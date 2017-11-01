package de.tototec.cmdoption;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;
import static de.tobiasroeser.lambdatest.Expect.expectFalse;
import static de.tobiasroeser.lambdatest.Expect.expectNull;
import static de.tobiasroeser.lambdatest.Expect.expectTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import de.tobiasroeser.lambdatest.junit.FreeSpec;

public class ExampleSemVerTest extends FreeSpec {

	static class Config {
		@CmdOption(names = { "--help", "-h" }, description = "Show this help and exit.", isHelp = true)
		boolean help;

		@CmdOption(names = { "--diff", "-d" }, conflictsWith = { "--check", "--infer",
		"--validate" }, description = "Show the differences between two jars.")
		public boolean diff;

		@CmdOption(names = { "--check", "-c" }, conflictsWith = { "--diff", "--infer",
		"--validate" }, description = "Check the compatibility of two jars.")
		public boolean check;

		@CmdOption(names = { "--infer", "-i" }, requires = { "--base-version" }, conflictsWith = { "--diff", "--check",
		"--validate" }, description = "Infer the version of the new jar based on the previous jar.")
		public boolean infer;

		@CmdOption(names = { "--validate", "-v" }, requires = { "--base-version", "--new-version" }, conflictsWith = {
				"--diff", "--check",
		"--infer" }, description = "Validate that the versions of two jars fulfil the semver specification.")
		public boolean validate;

		@CmdOption(names = { "--base-jar" }, args = { "JAR" }, minCount = 1, description = "The base jar.")
		public String baseJar;

		@CmdOption(names = { "--new-jar" }, args = { "JAR" }, minCount = 1, description = "The new jar.")
		public String newJar;

		final Set<String> includes = new LinkedHashSet<String>();

		@CmdOption(names = { "--includes" }, args = {
		"INCLUDE;..." }, description = "Semicolon separated list of full qualified class names to be included.")
		public void setIncludes(final String includes) {
			if (includes != null) {
				this.includes.addAll(Arrays.asList(includes.split(";")));
			}
		}

		final Set<String> excludes = new LinkedHashSet<String>();

		@CmdOption(names = { "--excludes" }, args = {
		"EXCLUDE;..." }, description = "Semicolon separated list of full qualified class names to be excluded.")
		public void setExcludes(final String excludes) {
			if (excludes != null) {
				this.excludes.addAll(Arrays.asList(excludes.split(";")));
			}
		}

		@CmdOption(names = { "--base-version" }, args = {
		"VERSION" }, description = "Version of the base jar (given with --base-jar).")
		public String baseVersion;

		@CmdOption(names = { "--new-version" }, args = {
		"VERSION" }, description = "Version of the new jar (given with --new-jar).")
		public String newVersion;
	}

	public ExampleSemVerTest() {
		setExpectFailFast(false);

		test("Parse commandline from semver version 0.9.16-SNAPSHOT", () -> {
			final Config config = new Config();
			final CmdlineParser cmdlineParser = new CmdlineParser(config);
			cmdlineParser.setProgramName("semver");
			cmdlineParser.setAboutLine("Semantic Version validator version 0.9.16-SNAPSHOT.");

			final String baseJar = "api/target/api-0.9.16-SNAPSHOT.jar";
			final String newJar = "api/target/api-0.9.16-SNAPSHOT.jar";

			final String[] args = { "--diff", "--base-jar", baseJar, "--new-jar", newJar };

			cmdlineParser.parse(args);

			expectTrue(config.diff);

			expectFalse(config.check);
			expectFalse(config.infer);
			expectFalse(config.validate);

			expectEquals(config.baseJar, baseJar);
			expectEquals(config.newJar, newJar);

			expectNull(config.baseVersion);
			expectNull(config.newVersion);

			expectEquals(config.includes, Collections.emptySet());
			expectEquals(config.excludes, Collections.emptySet());
		});
	}

}
