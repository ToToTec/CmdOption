package de.tototec.cmdoption.handler;

import static org.testng.Assert.assertEquals;
import de.tobiasroeser.lambdatest.testng.FreeSpec;
import de.tototec.cmdoption.CmdOption;
import de.tototec.cmdoption.CmdlineParser;
import de.tototec.cmdoption.CmdlineParserException;

public class LongHandlerTest extends FreeSpec {

	public static class LongConfig {
		@CmdOption(names = "-a", args = "N")
		private Long a;

		public Long getA() {
			return a;
		}

		private Long b;

		@CmdOption(names = "-b", args = "N")
		public void setUrlTwo(final Long b) {
			LongConfig.this.b = b;
		}

		public Long getB() {
			return b;
		}
	}

	public static class PLongConfig {
		@CmdOption(names = "-a", args = "N")
		private long a;

		public long getA() {
			return a;
		}

		private long b;

		@CmdOption(names = "-b", args = "N")
		public void setUrlTwo(final long b) {
			PLongConfig.this.b = b;
		}

		public long getB() {
			return b;
		}
	}

	public CmdlineParser cp() {
		final CmdlineParser cp = new CmdlineParser();
		cp.unregisterAllHandler();
		cp.registerHandler(new LongHandler());
		return cp;
	}

	{
		test("Long field", () -> {
			final LongConfig config = new LongConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("-a", "1");
			assertEquals(config.getA(), Long.valueOf(1));
			assertEquals(config.getB(), null);
		});

		test("Long field fail with non-integer", () -> {
			final LongConfig config = new LongConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			intercept(CmdlineParserException.class, "Could not read long value \"a\".", () -> {
				cp.parse("-a", "a");
			});
			assertEquals(config.getA(), null);
			assertEquals(config.getB(), null);
		});

		test("Long method", () -> {
			final LongConfig config = new LongConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("-b", "1");
			assertEquals(config.getA(), null);
			assertEquals(config.getB(), Long.valueOf(1));
		});

		test("Long method fail with non-integer", () -> {
			final LongConfig config = new LongConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			intercept(CmdlineParserException.class, "Could not read long value \"b\".", () -> {
				cp.parse("-b", "b");
			});
			assertEquals(config.getA(), null);
			assertEquals(config.getB(), null);
		});

		test("long field", () -> {
			final PLongConfig config = new PLongConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("-a", "1");
			assertEquals(config.getA(), 1);
			assertEquals(config.getB(), 0);
		});
		test("long field fail with non-integer", () -> {
			final PLongConfig config = new PLongConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			intercept(CmdlineParserException.class, "Could not read long value \"a\".", () -> {
				cp.parse("-a", "a");
			});
			assertEquals(config.getA(), 0);
			assertEquals(config.getB(), 0);
		});

		test("long method", () -> {
			final PLongConfig config = new PLongConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("-b", "1");
			assertEquals(config.getA(), 0);
			assertEquals(config.getB(), 1);
		});
		test("long method fail with non-integer", () -> {
			final PLongConfig config = new PLongConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			intercept(CmdlineParserException.class, "Could not read long value \"b\".", () -> {
				cp.parse("-b", "b");
			});
			assertEquals(config.getA(), 0);
			assertEquals(config.getB(), 0);
		});
	}

}
