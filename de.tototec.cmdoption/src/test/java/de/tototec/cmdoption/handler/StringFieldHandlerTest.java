package de.tototec.cmdoption.handler;

import static org.testng.Assert.assertEquals;
import de.tobiasroeser.lambdatest.testng.FreeSpec;
import de.tototec.cmdoption.CmdOption;
import de.tototec.cmdoption.CmdlineParser;
import de.tototec.cmdoption.CmdlineParserException;

public class StringFieldHandlerTest extends FreeSpec {

	public class ConfigStringField {
		@CmdOption(names = "--string1", args = "string")
		String string1;
	}

	public class ConfigFinalStringField {
		@CmdOption(names = "--string1", args = "string")
		final String string1 = "";
	}

	public CmdlineParser cp() {
		final CmdlineParser cp = new CmdlineParser();
		cp.unregisterAllHandler();
		cp.registerHandler(new StringFieldHandler());
		return cp;
	}

	{
		test("String field", () -> {
			final ConfigStringField config = new ConfigStringField();
			assertEquals(config.string1, null);

			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("--string1", "abc");
			assertEquals(config.string1, "abc");
		});
		test("String final field fail", () -> {
			final ConfigFinalStringField config = new ConfigFinalStringField();
			final CmdlineParser cp = cp();
			intercept(CmdlineParserException.class,
					"\\QNo suitable handler found for option(s): --string1 (1 argument(s))\\E", () -> {
						cp.addObject(config);
					});
		});
	}

}
