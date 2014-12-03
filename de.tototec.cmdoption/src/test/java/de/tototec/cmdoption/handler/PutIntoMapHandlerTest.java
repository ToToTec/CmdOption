package de.tototec.cmdoption.handler;

import static org.testng.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import de.tobiasroeser.lambdatest.testng.FreeSpec;
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
			assertEquals(config.map1.isEmpty(), true);

			final CmdlineParser cp = new CmdlineParser();
			cp.unregisterAllHandler();
			cp.registerHandler(new PutIntoMapHandler());
			cp.addObject(cp);
			cp.parse("--map1", "k", "v");
			assertEquals(config.map1.isEmpty(), false);
			assertEquals(config.map1.get("key"), "value");
		});
	}
}
