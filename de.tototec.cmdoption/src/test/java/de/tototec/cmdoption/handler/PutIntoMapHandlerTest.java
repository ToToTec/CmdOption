package de.tototec.cmdoption.handler;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import de.tobiasroeser.lambdatest.junit.FreeSpec;
import de.tototec.cmdoption.CmdOption;
import de.tototec.cmdoption.CmdlineParser;

public class PutIntoMapHandlerTest extends FreeSpec {

	static class Config1 {
		@CmdOption(names = { "--map1" }, args = { "key", "value" })
		final Map<String, String> map1 = new LinkedHashMap<String, String>();
	}

	{
		test("Config1: map1", () -> {
			final Config1 config = new Config1();
			expectEquals(config.map1.isEmpty(), true);

			final CmdlineParser cp = new CmdlineParser();
			cp.unregisterAllHandler();
			cp.registerHandler(new PutIntoMapHandler());
			cp.addObject(config);

			cp.parse("--map1", "k", "v");
			expectEquals(config.map1.isEmpty(), false);
			expectEquals(config.map1.get("k"), "v");
		});
	}
}
