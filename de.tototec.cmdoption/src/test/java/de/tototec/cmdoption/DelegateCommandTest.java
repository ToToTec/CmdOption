package de.tototec.cmdoption;

import static de.tobiasroeser.lambdatest.Expect.*;

import de.tobiasroeser.lambdatest.junit.FreeSpec;

public class DelegateCommandTest extends FreeSpec {

	@CmdCommand(names = {"cmd"})
	class Cmd {
		@CmdOption(names = {"-a"})
		private boolean a;
	}

	class Config {
		@CmdOptionDelegate(CmdOptionDelegate.Mode.FIND_COMMAND)
		private Cmd cmd = new Cmd();
	}

	public DelegateCommandTest() {

		test("Parse command", () -> {
			final Cmd cmd = new Cmd();
			expectFalse(cmd.a);
			final CmdlineParser cp = new CmdlineParser(cmd);
			cp.parse("cmd", "-a");
			expectEquals(cp.getParsedCommandName(), "cmd");
			expectTrue(cmd.a);
		});

		test("Parse embedded command", () -> {
			final Config config = new Config();
			expectFalse(config.cmd.a);
			final CmdlineParser cp = new CmdlineParser(config);
			cp.parse("cmd", "-a");
			expectEquals(cp.getParsedCommandName(), "cmd");
			expectTrue(config.cmd.a);
			expectEquals(cp.getParsedCommandObject(), config.cmd);

		});
	}

}
