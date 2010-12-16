package de.tobiasroeser.cmdoption.example;

import de.tobiasroeser.cmdoption.CmdOption;
import de.tobiasroeser.cmdoption.CmdOptionsParser;
import de.tobiasroeser.cmdoption.CmdOptionsParser.Result;

public class GreetingApp {

	public static class Config {

		@CmdOption(args = {"name"})
		public String myName = "Unknown";

	}

	public int run(String[] args) {
		Config config = new Config();
		CmdOptionsParser parser = new CmdOptionsParser(Config.class);
		Result result = parser.parseCmdline(args, config);
		if (result.isHelp()) {
			System.out.println(parser.formatOptions());
		}
		if (!result.isOk()) {
			System.err.println(result.message());
			return result.code();
		}

		System.out.println("Hello " + config.myName);
		return 0;
	}

	public static void main(String[] args) {
		int exitCode = new GreetingApp().run(args);
		System.exit(exitCode);
	}

}