package de.tototec.cmdoption;

import static org.testng.Assert.assertEquals;

import java.util.Locale;

import org.testng.annotations.Test;

public class FinalFieldTest {

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
	private final String expectedFinalUsage = "Usage: <main class>\n";

	@Test
	public void testField() {
		final Config config = new Config();
		final CmdlineParser cp = new CmdlineParser(config);
		final StringBuilder sb = new StringBuilder();
		cp.usage(sb);
		assertEquals(sb.toString(), expectedUsage);
		assertEquals(config.help, false);
		cp.parse("--help", "false");
		assertEquals(config.help, false);
		cp.parse("--help", "true");
		assertEquals(config.help, true);
	}

	@Test
	public void testFieldWithDefault() {
		final ConfigWithDefault config = new ConfigWithDefault();
		final CmdlineParser cp = new CmdlineParser(config);
		final StringBuilder sb = new StringBuilder();
		cp.usage(sb);
		assertEquals(sb.toString(), expectedUsage);
		assertEquals(config.help, false);
		cp.parse("--help", "false");
		assertEquals(config.help, false);
		cp.parse("--help", "true");
		assertEquals(config.help, true);
	}

	@Test(expectedExceptions = CmdlineParserException.class)
	public void testFinalFieldWithDefault() {
		final ConfigWithFinalField config = new ConfigWithFinalField();
		@SuppressWarnings("unused")
		final CmdlineParser cp = new CmdlineParser(config);
	}

	@Test(expectedExceptions = CmdlineParserException.class)
	public void testFinalFieldWithDefaultFail() {
		final ConfigWithFinalField config = new ConfigWithFinalField();
		final CmdlineParser cp = new CmdlineParser(config);
		final StringBuilder sb = new StringBuilder();
		cp.usage(sb);
		assertEquals(sb.toString(), expectedUsage);
		assertEquals(config.help, false);
		cp.parse("--help", "false");
	}

}
