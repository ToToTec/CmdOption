package de.tototec.cmdoption.handler;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;

import de.tobiasroeser.lambdatest.testng.FreeSpec;
import de.tototec.cmdoption.CmdOption;
import de.tototec.cmdoption.CmdlineParser;
import de.tototec.cmdoption.CmdlineParserException;

public class IntegerHandlerTest extends FreeSpec {

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

	public CmdlineParser cp() {
		final CmdlineParser cp = new CmdlineParser();
		cp.unregisterAllHandler();
		cp.registerHandler(new IntegerHandler());
		return cp;
	}

	{
		test("Integer field", () -> {
			final IntegerConfig config = new IntegerConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("-a", "1");
			expectEquals(config.getA(), Integer.valueOf(1));
			expectEquals(config.getB(), null);
		});

		test("Integer field fail with non-integer", () -> {
			final IntegerConfig config = new IntegerConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			intercept(CmdlineParserException.class, "Could not read integer value \"a\".", () -> {
				cp.parse("-a", "a");
			});
			expectEquals(config.getA(), null);
			expectEquals(config.getB(), null);
		});

		test("Integer method", () -> {
			final IntegerConfig config = new IntegerConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("-b", "1");
			expectEquals(config.getA(), null);
			expectEquals(config.getB(), Integer.valueOf(1));
		});

		test("Integer method fail with non-integer", () -> {
			final IntegerConfig config = new IntegerConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			intercept(CmdlineParserException.class, "Could not read integer value \"b\".", () -> {
				cp.parse("-b", "b");
			});
			expectEquals(config.getA(), null);
			expectEquals(config.getB(), null);
		});

		test("Int field", () -> {
			final IntConfig config = new IntConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("-a", "1");
			expectEquals(config.getA(), 1);
			expectEquals(config.getB(), 0);
		});
		test("Int field fail with non-integer", () -> {
			final IntConfig config = new IntConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			intercept(CmdlineParserException.class, "Could not read integer value \"a\".", () -> {
				cp.parse("-a", "a");
			});
			expectEquals(config.getA(), 0);
			expectEquals(config.getB(), 0);
		});

		test("Int method", () -> {
			final IntConfig config = new IntConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("-b", "1");
			expectEquals(config.getA(), 0);
			expectEquals(config.getB(), 1);
		});
		test("Int method fail with non-integer", () -> {
			final IntConfig config = new IntConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			intercept(CmdlineParserException.class, "Could not read integer value \"b\".", () -> {
				cp.parse("-b", "b");
			});
			expectEquals(config.getA(), 0);
			expectEquals(config.getB(), 0);
		});
	}

}
