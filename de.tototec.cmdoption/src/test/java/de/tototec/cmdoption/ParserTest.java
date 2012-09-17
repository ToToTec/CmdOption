package de.tototec.cmdoption;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ParserTest {

	@Test
	public void test1() {
		class Config {
		}
		CmdlineParser cp = new CmdlineParser(new Config());
		cp.parse();
	}

	@Test(expectedExceptions = CmdlineParserException.class, expectedExceptionsMessageRegExp = "Unsupported option or parameter found: --help")
	public void test2() {
		class Config {
		}
		CmdlineParser cp = new CmdlineParser(new Config());
		// should fail because we have no option --help defined
		cp.parse(new String[] { "--help" });
	}

	public static class Config3 {
		@CmdOption(names = "--help")
		public boolean help;
	}

	@Test
	public void test3() {
		Config3 config = new Config3();
		assertEquals(config.help, false);
		CmdlineParser cp = new CmdlineParser(config);
		cp.parse(new String[] { "--help" });
		assertEquals(config.help, true);
	}

	@Test(expectedExceptions = CmdlineParserException.class, expectedExceptionsMessageRegExp = "Unsupported option or parameter found: true")
	public void test3a() {
		CmdlineParser cp = new CmdlineParser(new Config3());
		cp.parse(new String[] { "--help", "true" });
	}

	@Test
	public void test3b() {
		CmdlineParser cp = new CmdlineParser(new Config3());
		StringBuilder sb = new StringBuilder();
		cp.usage(sb);
		System.out.println(sb);
		assertTrue(sb.length() > 10);
	}

	@Test(expectedExceptions = CmdlineParserException.class, expectedExceptionsMessageRegExp = "Option '--help' was given 2 times, but must be given between 0 and 1 times")
	public void test3c() {
		CmdlineParser cp = new CmdlineParser(new Config3());
		cp.parse(new String[] { "--help", "--help" });
	}

	public static class Config4 {
		@CmdOption(names = "--help", args = "true|false")
		public boolean help;
	}

	@Test
	public void test4() {
		Config4 config = new Config4();
		assertEquals(config.help, false);
		CmdlineParser cp = new CmdlineParser(config);
		cp.parse(new String[] { "--help", "true" });
		assertEquals(config.help, true);
	}

	@Test(expectedExceptions = CmdlineParserException.class)
	public void test4a() {
		CmdlineParser cp = new CmdlineParser(new Config4());
		cp.parse(new String[] { "--help" });
	}

	@Test
	public void test4b() {
		Config4 config = new Config4();
		CmdlineParser cp = new CmdlineParser(config);
		StringBuilder sb = new StringBuilder();
		cp.usage(sb);
		System.out.println(sb);
		assertTrue(sb.length() > 10);
		cp.parse(new String[] { "--help", "true" });
		assertEquals(config.help, true);
	}

	@Test(expectedExceptions = CmdlineParserException.class, expectedExceptionsMessageRegExp = "Could not parse argument '--help' as boolean parameter.")
	public void test4c() {
		CmdlineParser cp = new CmdlineParser(new Config4());
		cp.parse(new String[] { "--help", "--help" });
	}
}
