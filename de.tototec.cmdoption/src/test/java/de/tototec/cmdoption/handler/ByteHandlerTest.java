package de.tototec.cmdoption.handler;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;

import de.tobiasroeser.lambdatest.testng.FreeSpec;
import de.tototec.cmdoption.CmdOption;
import de.tototec.cmdoption.CmdlineParser;
import de.tototec.cmdoption.CmdlineParserException;

public class ByteHandlerTest extends FreeSpec {

	public static class ByteConfig {
		@CmdOption(names = "-a", args = "N")
		private Byte a;

		public Byte getA() {
			return a;
		}

		private Byte b;

		@CmdOption(names = "-b", args = "N")
		public void setUrlTwo(final Byte b) {
			ByteConfig.this.b = b;
		}

		public Byte getB() {
			return b;
		}
	}

	public static class PByteConfig {
		@CmdOption(names = "-a", args = "N")
		private byte a;

		public byte getA() {
			return a;
		}

		private byte b;

		@CmdOption(names = "-b", args = "N")
		public void setUrlTwo(final byte b) {
			PByteConfig.this.b = b;
		}

		public byte getB() {
			return b;
		}
	}

	public CmdlineParser cp() {
		final CmdlineParser cp = new CmdlineParser();
		cp.unregisterAllHandler();
		cp.registerHandler(new ByteHandler());
		return cp;
	}

	{
		test("Byte field", () -> {
			final ByteConfig config = new ByteConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("-a", "1");
			expectEquals(config.getA(), Byte.valueOf((byte) 1));
			expectEquals(config.getB(), null);
		});

		test("Byte field fail with non-integer", () -> {
			final ByteConfig config = new ByteConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			intercept(CmdlineParserException.class, "Could not read byte value \"a\".", () -> {
				cp.parse("-a", "a");
			});
			expectEquals(config.getA(), null);
			expectEquals(config.getB(), null);
		});

		test("Byte method", () -> {
			final ByteConfig config = new ByteConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("-b", "1");
			expectEquals(config.getA(), null);
			expectEquals(config.getB(), Byte.valueOf((byte) 1));
		});

		test("Byte method fail with non-integer", () -> {
			final ByteConfig config = new ByteConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			intercept(CmdlineParserException.class, "Could not read byte value \"b\".", () -> {
				cp.parse("-b", "b");
			});
			expectEquals(config.getA(), null);
			expectEquals(config.getB(), null);
		});

		test("byte field", () -> {
			final PByteConfig config = new PByteConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("-a", "1");
			expectEquals(config.getA(), (byte) 1);
			expectEquals(config.getB(), (byte) 0);
		});
		test("byte field fail with non-integer", () -> {
			final PByteConfig config = new PByteConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			intercept(CmdlineParserException.class, "Could not read byte value \"a\".", () -> {
				cp.parse("-a", "a");
			});
			expectEquals(config.getA(), (byte) 0);
			expectEquals(config.getB(), (byte) 0);
		});

		test("byte method", () -> {
			final PByteConfig config = new PByteConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("-b", "1");
			expectEquals(config.getA(), (byte) 0);
			expectEquals(config.getB(), (byte) 1);
		});
		test("byte method fail with non-integer", () -> {
			final PByteConfig config = new PByteConfig();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			intercept(CmdlineParserException.class, "Could not read byte value \"b\".", () -> {
				cp.parse("-b", "b");
			});
			expectEquals(config.getA(), (byte) 0);
			expectEquals(config.getB(), (byte) 0);
		});
	}

}
