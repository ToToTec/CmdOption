package de.tototec.cmdoption;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import de.tobiasroeser.lambdatest.junit.FreeSpec;

public class FinalFieldTest extends FreeSpec {

	public static class Config {
		@CmdOption(names = "--help", args = "true|false")
		private boolean help;
	}

	public static class ConfigWithDefault {
		@CmdOption(names = "--help", args = "true|false")
		private boolean help = false;
	}

	public static class ConfigWithFinalField {
		@CmdOption(names = "--help", args = "true|false")
		private final boolean help = false;
	}

	private final String expectedUsage = "Usage: <main class> [options]\n\nOptions:\n  --help true|false  \n";

	public FinalFieldTest() {

		test("non-final private field, uninitialized", () -> {
			final Config config = new Config();
			final CmdlineParser cp = new CmdlineParser(config);
			cp.setUsageFormatter(new DefaultUsageFormatter2(true, 80));
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final PrintStream ps = new PrintStream(baos);
			cp.usage(ps);
			final String usage = new String(baos.toByteArray(), Charset.forName("UTF-8"));
			expectEquals(usage, expectedUsage);
			expectEquals(config.help, false);
			cp.parse("--help", "false");
			expectEquals(config.help, false);
			cp.parse("--help", "true");
			expectEquals(config.help, true);
		});

		test("non-final private field, initialized", () -> {
			final ConfigWithDefault config = new ConfigWithDefault();
			final CmdlineParser cp = new CmdlineParser(config);
			cp.setUsageFormatter(new DefaultUsageFormatter2(true, 80));
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final PrintStream ps = new PrintStream(baos);
			cp.usage(ps);
			final String usage = new String(baos.toByteArray(), Charset.forName("UTF-8"));
			baos.toString();
			expectEquals(usage, expectedUsage);
			expectEquals(config.help, false);
			cp.parse("--help", "false");
			expectEquals(config.help, false);
			cp.parse("--help", "true");
			expectEquals(config.help, true);
		});

		test("final private field, uninitialized", () -> {

			final ConfigWithFinalField config = new ConfigWithFinalField();
			intercept(CmdlineParserException.class, () -> {
				@SuppressWarnings("unused")
				final CmdlineParser cp = new CmdlineParser(config);
			});
		});

	}
}
