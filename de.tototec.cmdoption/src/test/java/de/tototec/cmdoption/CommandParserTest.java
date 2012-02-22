package de.tototec.cmdoption;

public class CommandParserTest {

	public static class Config {
		@CmdOption(names = "--help")
		public boolean help;
		@CmdOption(names = { "--param", "-p" }, args = "VALUE", description = "Test Parameter")
		public String param;
	}

	@CmdCommand(names = "--cmd-one", description = "Command One")
	public static class CommandOne {
		@CmdOption(names = "--name", args = "name")
		public String cmdName;
	}

	public static void main(String[] args) {
		Config config = new Config();
		CommandOne cmd1 = new CommandOne();
		CmdlineParser cp = new CmdlineParser(config, cmd1);
		cp.usage();

		cp.parse(true, false, new String[] { "--help" });
		cp.parse(true, false, new String[] { "--param", "test" });
		// cp.parse(true, false, new String[] { "--cmd-one", "--help" });
		cp.parse(true, false, new String[] { "--help", "--cmd-one" });
		// cp.parse(true, false, new String[] { "--cmd-one", "--name" });
		cp.parse(true, false, new String[] { "--cmd-one", "--name", "name" });
	}
}
