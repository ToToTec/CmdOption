package de.tototec.cmdoption;

import static org.testng.Assert.assertEquals;

import java.io.File;

import java.io.PrintWriter;

import de.tobiasroeser.lambdatest.testng.FreeSpec;

public class ParamsFromFileTest extends FreeSpec {

	static class Config {
		@CmdOption(names = "--arg1", args = "arg")
		String arg1;
		@CmdOption(names = "--arg2", args = "arg")
		String arg2;
	}

	{
		test("Read some arguments from file", () -> {
			final File file = File.createTempFile("test", "");
			final PrintWriter writer = new PrintWriter(file);
			writer.println("--arg2");
			writer.println("def");
			writer.close();

			final Config config = new Config();
			final CmdlineParser cp = new CmdlineParser(config);
			cp.parse("--arg1", "abc", "@" + file.getAbsolutePath());

			assertEquals(config.arg1, "abc");
			assertEquals(config.arg2, "def");
		});

	}
}
