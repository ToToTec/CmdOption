package de.tototec.cmdoption;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TranslationTutorialMain {

	public static class Config {
		@CmdOption(names = { "--help", "-h" }, description = "Show this help.", isHelp = true)
		public boolean help;

		@CmdOption(names = { "--verbose", "-v" }, description = "Be more verbose.")
		private boolean verbose;

		@CmdOption(names = { "--options", "-o" }, args = { "name",
		"value" }, maxCount = -1, description = "Additional options when processing names.")
		private final Map<String, String> options = new LinkedHashMap<String, String>();

		@CmdOption(args = { "file" }, description = "Names to process.", minCount = 1, maxCount = -1)
		private final List<String> names = new LinkedList<String>();
	}

	public static void main(final String[] args) {
		final Config config = new Config();
		final CmdlineParser cp = new CmdlineParser(config);
		cp.setDebugMode(true);
		cp.setResourceBundle(TranslationTutorialMain.class.getName() + "_Messages",
				TranslationTutorialMain.class.getClassLoader());
		cp.setProgramName("myprogram");
		cp.setAboutLine("Example names processor v1.0");

		try {
			cp.parse(args);
		} catch (final CmdlineParserException e) {
			System.err.println("Error: " + e.getLocalizedMessage() + "\nRun myprogram --help for help.");
			System.exit(1);
		}

		if (config.help) {
			cp.usage();
			return;
		}

		// ...
	}

}
