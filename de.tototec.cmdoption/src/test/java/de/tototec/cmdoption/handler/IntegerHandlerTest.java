package de.tototec.cmdoption.handler;

import org.testng.Assert;
import org.testng.annotations.Test;

import de.tototec.cmdoption.CmdOption;
import de.tototec.cmdoption.CmdlineParser;
import de.tototec.cmdoption.CmdlineParserException;

public class IntegerHandlerTest {

	public static class IntegerConfig {
		@CmdOption(names = "-a", args = "N")
		private Integer a;

		public Integer getA() {
			return a;
		}

		private Integer b;

		@CmdOption(names = "-b", args = "N")
		public void setUrlTwo(final Integer b) {
			IntegerConfig.this.b = b;
		}

		public Integer getB() {
			return b;
		}
	}

	@Test
	public void testIntegerField() {
		final IntegerConfig config = new IntegerConfig();
		final CmdlineParser cp = new CmdlineParser(config);
		cp.unregisterAllHandler();
		cp.registerHandler(new IntegerHandler());
		cp.parse("-a", "1");

		Assert.assertEquals(config.getA(), Integer.valueOf(1));
		Assert.assertEquals(config.getB(), null);
	}

	@Test(expectedExceptions = CmdlineParserException.class)
	public void testIntegerFieldFail() {
		final IntegerConfig config = new IntegerConfig();
		final CmdlineParser cp = new CmdlineParser(config);
		cp.unregisterAllHandler();
		cp.registerHandler(new IntegerHandler());
		cp.parse("-a", "a");
	}

	@Test
	public void testIntegerMethod() {
		final IntegerConfig config = new IntegerConfig();
		final CmdlineParser cp = new CmdlineParser(config);
		cp.unregisterAllHandler();
		cp.registerHandler(new IntegerHandler());
		cp.parse("-b", "1");

		Assert.assertEquals(config.getB(), Integer.valueOf(1));
		Assert.assertEquals(config.getA(), null);
	}

	@Test(expectedExceptions = CmdlineParserException.class)
	public void testIntegerMethodFail() {
		final IntegerConfig config = new IntegerConfig();
		final CmdlineParser cp = new CmdlineParser(config);
		cp.unregisterAllHandler();
		cp.registerHandler(new IntegerHandler());
		cp.parse("-b", "b");
	}

	public static class IntConfig {
		@CmdOption(names = "-a", args = "N")
		private int a;

		public int getA() {
			return a;
		}

		private int b;

		@CmdOption(names = "-b", args = "N")
		public void setUrlTwo(final int b) {
			IntConfig.this.b = b;
		}

		public int getB() {
			return b;
		}
	}

	@Test
	public void testIntField() {
		final IntConfig config = new IntConfig();
		final CmdlineParser cp = new CmdlineParser(config);
		cp.unregisterAllHandler();
		cp.registerHandler(new IntegerHandler());
		cp.parse("-a", "1");

		Assert.assertEquals(config.getA(), 1);
		Assert.assertEquals(config.getB(), 0);
	}

	@Test(expectedExceptions = CmdlineParserException.class)
	public void testIntFieldFail() {
		final IntConfig config = new IntConfig();
		final CmdlineParser cp = new CmdlineParser(config);
		cp.unregisterAllHandler();
		cp.registerHandler(new IntegerHandler());
		cp.parse("-a", "a");
	}

	@Test
	public void testIntMethod() {
		final IntConfig config = new IntConfig();
		final CmdlineParser cp = new CmdlineParser(config);
		cp.unregisterAllHandler();
		cp.registerHandler(new IntegerHandler());
		cp.parse("-b", "1");

		Assert.assertEquals(config.getB(), 1);
		Assert.assertEquals(config.getA(), 0);
	}

	@Test(expectedExceptions = CmdlineParserException.class)
	public void testIntMethodFail() {
		final IntConfig config = new IntConfig();
		final CmdlineParser cp = new CmdlineParser(config);
		cp.unregisterAllHandler();
		cp.registerHandler(new IntegerHandler());
		cp.parse("-b", "b");
	}

}
