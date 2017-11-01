package de.tototec.cmdoption;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;

import java.io.File;
import java.io.PrintWriter;

import de.tobiasroeser.lambdatest.junit.FreeSpec;

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

			expectEquals(config.arg1, "abc");
			expectEquals(config.arg2, "def");
		});

	}
}
