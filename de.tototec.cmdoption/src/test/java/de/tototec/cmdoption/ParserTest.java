package de.tototec.cmdoption;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import org.testng.annotations.Test;

public class ParserTest {

	@Test
	public void testParseEmptyConfig() {
		class Config {}
		final CmdlineParser cp = new CmdlineParser(new Config());
		cp.parse();
	}

	@Test(expectedExceptions = CmdlineParserException.class, expectedExceptionsMessageRegExp = "Unsupported option or parameter found: --help")
	public void testParseHelpFail() {
		class Config {}
		final CmdlineParser cp = new CmdlineParser(new Config());
		// should fail because we have no option --help defined
		cp.parse(new String[] { "--help" });
	}

	public static class Config3 {
		@CmdOption(names = "--help")
		public boolean help;
	}

	@Test
	public void testParseHelp() {
		final Config3 config = new Config3();
		assertEquals(config.help, false);
		final CmdlineParser cp = new CmdlineParser(config);
		cp.parse(new String[] { "--help" });
		assertEquals(config.help, true);
	}

	@Test(expectedExceptions = CmdlineParserException.class, expectedExceptionsMessageRegExp = "Unsupported option or parameter found: true")
	public void testOptionWithOrphanArgFail() {
		final CmdlineParser cp = new CmdlineParser(new Config3());
		cp.parse(new String[] { "--help", "true" });
	}

	@Test
	public void testPrintUsageContainsSomeCharacters() {
		final CmdlineParser cp = new CmdlineParser(new Config3());
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos);
		cp.usage(ps);
		final String usage = new String(baos.toByteArray(), Charset.forName("UTF-8"));
		System.out.println(usage);
		assertTrue(usage.length() > 10);
	}

	@Test(expectedExceptions = CmdlineParserException.class, expectedExceptionsMessageRegExp = "Option \"--help\" was given 2 times, but must be given between 0 and 1 times")
	public void testParseOptionalHelpTwiceFail() {
		final CmdlineParser cp = new CmdlineParser(new Config3());
		cp.parse(new String[] { "--help", "--help" });
	}

	public static class Config3d {
		@CmdOption(names = "--help", minCount = 1)
		public boolean help;
	}

	@Test(expectedExceptions = CmdlineParserException.class, expectedExceptionsMessageRegExp = "Option \"--help\" was given 2 times, but must be given exactly 1 times")
	public void testParseRequiredHelpTwiceFail() {
		final CmdlineParser cp = new CmdlineParser(new Config3d());
		cp.parse(new String[] { "--help", "--help" });
	}

	@Test(expectedExceptions = CmdlineParserException.class, expectedExceptionsMessageRegExp = "Option \"--help\" was given 0 times, but must be given exactly 1 times")
	public void testParseRequiredHelpMissingFail() {
		final CmdlineParser cp = new CmdlineParser(new Config3d());
		cp.parse(new String[] {});
	}

	public static class Config4 {
		@CmdOption(names = "--help", args = "true|false")
		public boolean help;
	}

	@Test
	public void testChangedHelpFieldAfterParse() {
		final Config4 config = new Config4();
		assertEquals(config.help, false);
		final CmdlineParser cp = new CmdlineParser(config);
		cp.parse(new String[] { "--help", "true" });
		assertEquals(config.help, true);
	}

	@Test(expectedExceptions = CmdlineParserException.class)
	public void testParseOneArgOptionWithoutArgFail() {
		final CmdlineParser cp = new CmdlineParser(new Config4());
		cp.parse(new String[] { "--help" });
	}

	@Test
	public void testPrintUsageAndParseHelp() {
		final Config4 config = new Config4();
		final CmdlineParser cp = new CmdlineParser(config);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos);
		cp.usage(ps);
		final String usage = new String(baos.toByteArray(), Charset.forName("UTF-8"));
		System.out.println(usage);
		assertTrue(usage.length() > 10);
		cp.parse(new String[] { "--help", "true" });
		assertEquals(config.help, true);
	}

	@Test(expectedExceptions = CmdlineParserException.class, expectedExceptionsMessageRegExp = "Could not parse argument \"--help\" as boolean parameter.")
	public void testParseHelpTwiceFail() {
		final CmdlineParser cp = new CmdlineParser(new Config4());
		cp.parse(new String[] { "--help", "--help" });
	}

	public static class Config5 {
		@CmdOption(names = "--opt", requires = { "--reqA", "--reqB" })
		public boolean opt;

		@CmdOption(names = "--reqA")
		public boolean reqA;
	}

	@Test(expectedExceptions = CmdlineParserException.class, expectedExceptionsMessageRegExp = "^The option \"--opt\" requires the unknown/missing option \"--reqB\".$")
	public void testParseTwoOptionWhichRequiresTwoOtherButOneIsMissingFail() {
		final Config5 config = new Config5();
		final CmdlineParser cp = new CmdlineParser(config);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos);
		cp.usage(ps);
		final String usage = new String(baos.toByteArray(), Charset.forName("UTF-8"));
		assertTrue(usage.length() > 10);
		cp.parse(new String[] { "--opt", "--reqA" });
	}

	@Test(expectedExceptions = CmdlineParserException.class, expectedExceptionsMessageRegExp = "^The option \"--opt\" requires the unknown/missing option \"--reqB\".$")
	public void testParseOneOptionWhichRequiresTwoOthersButOneIsMissingFail() {
		final Config5 config = new Config5();
		final CmdlineParser cp = new CmdlineParser(config);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos);
		cp.usage(ps);
		final String usage = new String(baos.toByteArray(), Charset.forName("UTF-8"));
		assertTrue(usage.length() > 10);
		cp.parse(new String[] { "--opt" });
	}

	public static class Config6 {
		@CmdOption(names = "--opt", requires = { "--reqA" })
		public boolean opt;

		@CmdOption(names = "--reqA")
		public boolean reqA;
	}

	@Test(expectedExceptions = CmdlineParserException.class, expectedExceptionsMessageRegExp = "^When using option \"--opt\" also option \"--reqA\" must be given.$")
	public void testParseOptionWhichRequiresAnotherOneFail() {
		final Config6 config = new Config6();
		final CmdlineParser cp = new CmdlineParser(config);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos);
		cp.usage(ps);
		final String usage = new String(baos.toByteArray(), Charset.forName("UTF-8"));
		assertTrue(usage.length() > 10);
		cp.parse(new String[] { "--opt" });
	}

}
