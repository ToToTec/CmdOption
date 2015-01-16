import static org.testng.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tobiasroeser.lambdatest.testng.FreeSpec;
import de.tototec.cmdoption.CmdOption;
import de.tototec.cmdoption.CmdlineParser;

/**
 * https://github.com/ToToTec/CmdOption/issues/12
 */
public class T12NpeInDefaultPackage extends FreeSpec {

	public static class Config {
		@CmdOption(names = { "--help", "-h" }, description = "Show this help.", isHelp = true)
		public boolean help;

		@CmdOption(names = { "--verbose", "-v" }, description = "Be more verbose.")
		private boolean verbose;

		@CmdOption(names = { "--options", "-o" }, args = { "name", "value" }, maxCount = -1, description = "Additional options when processing names.")
		private final Map<String, String> options = new LinkedHashMap<String, String>();

		@CmdOption(args = { "file" }, description = "Names to process.", minCount = 1, maxCount = -1)
		private final List<String> names = new LinkedList<String>();

	}

	{
		test("Create Config class from DefaultPackage", () -> {
			final Config config = new Config();
			final CmdlineParser cp = new CmdlineParser(config);
			cp.parse("-h");
			assertEquals(config.help, true);
		});
	}

}
