package de.tototec.cmdoption;

import static de.tobiasroeser.lambdatest.Expect.expectFalse;
import static de.tobiasroeser.lambdatest.Expect.expectTrue;

import de.tobiasroeser.lambdatest.testng.FreeSpec;

public class DelegateTest extends FreeSpec {

	class ConfigA {
		@CmdOption(names = "-a")
		private boolean a;
	}

	class ConfigB {
		@CmdOptionDelegate
		private ConfigA a = new ConfigA();
	}

	public DelegateTest() {

		test("Parse option", () -> {
			final ConfigA config = new ConfigA();
			expectFalse(config.a);
			final CmdlineParser cp = new CmdlineParser(config);
			cp.parse("-a");
			expectTrue(config.a);
		});

		test("Parse @CmdDelegateOption", () -> {
			final ConfigB config = new ConfigB();
			expectFalse(config.a.a);
			final CmdlineParser cp = new CmdlineParser(config);
			cp.parse("-a");
			expectTrue(config.a.a);

		});
	}

}
