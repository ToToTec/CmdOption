package de.tototec.cmdoption.handler;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;

import de.tobiasroeser.lambdatest.junit.FreeSpec;
import de.tototec.cmdoption.CmdOption;
import de.tototec.cmdoption.CmdlineParser;

public class StringMethodHandlerTest extends FreeSpec {

	public class ConfigStringMethod {
		@CmdOption(names = "--string1", args = "string")
		public void setString1(final String string) {
			string1 = string;
		}

		String string1;

		@CmdOption(names = "--string2", args = { "a", "b" })
		public void setString2(final String a, final String b) {
			string2a = a;
			string2b = b;
		}

		String string2a;
		String string2b;

		@CmdOption(names = "--string3", args = { "a", "b", "c" })
		public void setString3(final String a, final String b, final String c) {
			string3a = a;
			string3b = b;
			string3c = c;
		}

		String string3a;
		String string3b;
		String string3c;
	}

	public CmdlineParser cp() {
		final CmdlineParser cp = new CmdlineParser();
		cp.unregisterAllHandler();
		cp.registerHandler(new StringMethodHandler());
		return cp;
	}

	{
		test("String method", () -> {
			final ConfigStringMethod config = new ConfigStringMethod();
			expectEquals(config.string1, null);

			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("--string1", "abc");
			expectEquals(config.string1, "abc");
		});

		test("String method with 2 params", () -> {
			final ConfigStringMethod config = new ConfigStringMethod();
			expectEquals(config.string2a, null);
			expectEquals(config.string2b, null);

			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("--string2", "abc", "def");
			expectEquals(config.string2a, "abc");
			expectEquals(config.string2b, "def");
		});

		test("String method with 3 params", () -> {
			final ConfigStringMethod config = new ConfigStringMethod();
			expectEquals(config.string3a, null);
			expectEquals(config.string3b, null);
			expectEquals(config.string3c, null);

			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("--string3", "abc", "def", "ghi");
			expectEquals(config.string3a, "abc");
			expectEquals(config.string3b, "def");
			expectEquals(config.string3c, "ghi");

		});
	}

}
