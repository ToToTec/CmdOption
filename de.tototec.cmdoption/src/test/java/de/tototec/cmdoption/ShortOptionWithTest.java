package de.tototec.cmdoption;

import de.tobiasroeser.lambdatest.testng.FreeSpec;

import static de.tobiasroeser.lambdatest.Expect.*;

public class ShortOptionWithTest extends FreeSpec {

	public static class Config {
		@CmdOption(names = {"-f", "--file"}, args = {"FILE"})
		String file = null;

		@CmdOption(names = {"-c", "--count"}, args = {"n"})
		int count = 0;
	}

	public static class Param {
		@CmdOption(args = {"PARAMETER"})
		String param;
	}

	public ShortOptionWithTest() {

		test("Setting all short options separately should work (reference test)", () -> {
			final Config config = new Config();
			final CmdlineParser cp = new CmdlineParser(config);
			cp.parse(new String[]{"-c", "4", "-f", "file.txt"});
			expectEquals(config.count, 4);
			expectEquals(config.file, "file.txt");
		});

		test("Setting all short options separately should work (when feature is enabled)", () -> {
			final Config config = new Config();
			final CmdlineParser cp = new CmdlineParser(config);
			cp.setShortOptionWithArgsPrefix("-");
			cp.parse(new String[]{"-c", "4", "-f", "file.txt"});
			expectEquals(config.count, 4);
			expectEquals(config.file, "file.txt");
		});

		test("Short option with arg is not supported when disabled", () -> {
			final Config config = new Config();
			final CmdlineParser cp = new CmdlineParser(config);
			intercept(CmdlineParserException.class, "\\QUnsupported option or parameter found: -c4\\E", () -> {
				cp.parse(new String[]{"-c4"});
			});
		});

		test("Short option with arg should work", () -> {
			final Config config = new Config();
			final CmdlineParser cp = new CmdlineParser(config);
			cp.setShortOptionWithArgsPrefix("-");
			cp.parse(new String[]{"--CMDOPTION_DEBUG", "-c4", "-ffile.txt"});
			expectEquals(config.count, 4);
			expectEquals(config.file, "file.txt");
		});

	}

}
