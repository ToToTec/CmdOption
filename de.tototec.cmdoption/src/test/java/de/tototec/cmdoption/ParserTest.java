package de.tototec.cmdoption;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;
import static de.tobiasroeser.lambdatest.Expect.expectTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import de.tobiasroeser.lambdatest.testng.FreeSpec;

public class ParserTest extends FreeSpec {

	public static class Config3 {
		@CmdOption(names = "--help")
		public boolean help;
	}

	public static class Config3d {
		@CmdOption(names = "--help", minCount = 1)
		public boolean help;
	}

	public static class Config4 {
		@CmdOption(names = "--help", args = "true|false")
		public boolean help;
	}

	public static class Config5 {
		@CmdOption(names = "--opt", requires = { "--reqA", "--reqB" })
		public boolean opt;

		@CmdOption(names = "--reqA")
		public boolean reqA;
	}

	public static class Config6 {
		@CmdOption(names = "--opt", requires = { "--reqA" })
		public boolean opt;

		@CmdOption(names = "--reqA")
		public boolean reqA;
	}

	public ParserTest() {

		test("Parse empty config", () -> {
			class Config {}
			final CmdlineParser cp = new CmdlineParser(new Config());
			cp.parse();
		});

		test("Parse unsupported help option should fail", () -> {
			class Config {}
			final CmdlineParser cp = new CmdlineParser(new Config());
			intercept(CmdlineParserException.class, "\\QUnsupported option or parameter found: --help\\E", () -> {
				// should fail because we have no option --help defined
				cp.parse(new String[] { "--help" });
			});
		});

		test("Parse help", () -> {
			final Config3 config = new Config3();
			expectEquals(config.help, false);
			final CmdlineParser cp = new CmdlineParser(config);
			cp.parse(new String[] { "--help" });
			expectEquals(config.help, true);
		});

		test("Parse unsupported help option should fail", () -> {
			final CmdlineParser cp = new CmdlineParser(new Config3());
			intercept(CmdlineParserException.class, "\\QUnsupported option or parameter found: true\\E", () -> {
				cp.parse(new String[] { "--help", "true" });
			});
		});

		test("Print unsage contains some characters", () -> {
			final CmdlineParser cp = new CmdlineParser(new Config3());
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final PrintStream ps = new PrintStream(baos);
			cp.usage(ps);
			final String usage = new String(baos.toByteArray(), Charset.forName("UTF-8"));
			System.out.println(usage);
			expectTrue(usage.length() > 10);
		});

		test("Parse optional help twice should fail", () -> {
			final CmdlineParser cp = new CmdlineParser(new Config3());
			intercept(CmdlineParserException.class,
					"\\QOption \"--help\" was given 2 times, but must be given between 0 and 1 times\\E",
					() -> {
						cp.parse(new String[] { "--help", "--help" });
					});
		});

		test("Parse required help twice should fail", () -> {
			final CmdlineParser cp = new CmdlineParser(new Config3d());
			intercept(CmdlineParserException.class,
					"\\QOption \"--help\" was given 2 times, but must be given exactly 1 times\\E",
					() -> {
						cp.parse(new String[] { "--help", "--help" });
					});
		});

		test("Parse required help missing should fail", () -> {
			final CmdlineParser cp = new CmdlineParser(new Config3d());
			intercept(CmdlineParserException.class,
					"\\QOption \"--help\" was given 0 times, but must be given exactly 1 times\\E",
					() -> {
						cp.parse(new String[] {});
					});
		});

		test("Changed help field after parse", () -> {
			final Config4 config = new Config4();
			expectEquals(config.help, false);
			final CmdlineParser cp = new CmdlineParser(config);
			cp.parse(new String[] { "--help", "true" });
			expectEquals(config.help, true);
		});

		test("Parse one arg option without arg should fail", () -> {
			final CmdlineParser cp = new CmdlineParser(new Config4());
			intercept(CmdlineParserException.class, () -> {
				cp.parse(new String[] { "--help" });
			});
		});

		test("Print usage and parse help", () -> {
			final Config4 config = new Config4();
			final CmdlineParser cp = new CmdlineParser(config);
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final PrintStream ps = new PrintStream(baos);
			cp.usage(ps);
			final String usage = new String(baos.toByteArray(), Charset.forName("UTF-8"));
			System.out.println(usage);
			expectTrue(usage.length() > 10);
			cp.parse(new String[] { "--help", "true" });
			expectEquals(config.help, true);
		});

		test("Parse help twice should fail", () -> {
			final CmdlineParser cp = new CmdlineParser(new Config4());
			intercept(CmdlineParserException.class,
					"\\QCould not parse argument \"--help\" as boolean parameter.\\E",
					() -> {
						cp.parse(new String[] { "--help", "--help" });
					});
		});

		test("Parse two option which requires two but one is missing should fail", () -> {
			final Config5 config = new Config5();
			final CmdlineParser cp = new CmdlineParser(config);
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final PrintStream ps = new PrintStream(baos);
			cp.usage(ps);
			final String usage = new String(baos.toByteArray(), Charset.forName("UTF-8"));
			expectTrue(usage.length() > 10);
			intercept(CmdlineParserException.class,
					"^\\QThe option \"--opt\" requires the unknown/missing option \"--reqB\".\\E$",
					() -> {
						cp.parse(new String[] { "--opt", "--reqA" });
					});
		});

		test("Parse two option which requires two others but one is missing should fail", () -> {
			final Config5 config = new Config5();
			final CmdlineParser cp = new CmdlineParser(config);
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final PrintStream ps = new PrintStream(baos);
			cp.usage(ps);
			final String usage = new String(baos.toByteArray(), Charset.forName("UTF-8"));
			expectTrue(usage.length() > 10);
			intercept(CmdlineParserException.class,
					"^\\QThe option \"--opt\" requires the unknown/missing option \"--reqB\".\\E$",
					() -> {
						cp.parse(new String[] { "--opt" });
					});
		});

		test("Parse option which requires another one should fail", () -> {
			final Config6 config = new Config6();
			final CmdlineParser cp = new CmdlineParser(config);
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final PrintStream ps = new PrintStream(baos);
			cp.usage(ps);
			final String usage = new String(baos.toByteArray(), Charset.forName("UTF-8"));
			expectTrue(usage.length() > 10);
			intercept(CmdlineParserException.class,
					"^\\QWhen using option \"--opt\" also option \"--reqA\" must be given.\\E$",
					() -> {
						cp.parse(new String[] { "--opt" });
					});
		});

	}

}
