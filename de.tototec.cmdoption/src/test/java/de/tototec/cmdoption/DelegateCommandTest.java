package de.tototec.cmdoption;

import static de.tobiasroeser.lambdatest.Expect.*;

import de.tobiasroeser.lambdatest.testng.FreeSpec;

public class DelegateCommandTest extends FreeSpec {

	@CmdCommand(names = {"cmd"})
	class Cmd {
		@CmdOption(names = {"-a"})
		private boolean a;
	}

	class Config {
		@CmdOptionDelegate(CmdOptionDelegate.Mode.COMMAND)
		private Cmd cmd = new Cmd();
	}


	class ConfigWithoutCmd {
		@CmdOptionDelegate(CmdOptionDelegate.Mode.OPTIONS)
		private Cmd cmd = new Cmd();
	}

	class ConfigWithCmd {
		@CmdOptionDelegate(CmdOptionDelegate.Mode.COMMAND_OR_OPTIONS)
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

		test("Parse delegate options from command", () -> {
			final ConfigWithoutCmd config = new ConfigWithoutCmd();
			expectFalse(config.cmd.a);
			final CmdlineParser cp = new CmdlineParser(config);
			cp.parse("-a");
			expectEquals(cp.getParsedCommandName(), null);
			expectTrue(config.cmd.a);
			expectEquals(cp.getParsedCommandObject(), null);
		});

		test("Parse embedded with explicit command", () -> {
			final ConfigWithCmd config = new ConfigWithCmd();
			expectFalse(config.cmd.a);
			final CmdlineParser cp = new CmdlineParser(config);
			cp.parse("cmd", "-a");
			expectEquals(cp.getParsedCommandName(), "cmd");
			expectTrue(config.cmd.a);
			expectEquals(cp.getParsedCommandObject(), config.cmd);
		});
	}

}
