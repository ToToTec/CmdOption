package de.tototec.cmdoption;

import java.util.Locale;

import org.testng.Assert;
import org.testng.annotations.Test;

public class FinalFieldTest extends Assert {

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

	private void assertUsage(final CmdlineParser cp, final String expectedUsage) {
		final StringBuilder sb = new StringBuilder();
		final Locale defaultLocale = Locale.getDefault();
		try {
			Locale.setDefault(Locale.ROOT);
			cp.usage(sb);
		} finally {
			Locale.setDefault(defaultLocale);
		}
		assertEquals(sb.toString(), expectedUsage);
	}

	@Test
	public void testField() {
		final Config config = new Config();
		final CmdlineParser cp = new CmdlineParser(config);
		assertUsage(cp, expectedUsage);
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
		assertUsage(cp, expectedUsage);
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
		assertUsage(cp, expectedFinalUsage);
		assertEquals(config.help, false);
		cp.parse("--help", "false");
	}

}
