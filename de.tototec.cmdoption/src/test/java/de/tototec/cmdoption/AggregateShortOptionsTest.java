package de.tototec.cmdoption;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;
import static de.tobiasroeser.lambdatest.Expect.expectFalse;
import static de.tobiasroeser.lambdatest.Expect.expectTrue;

import de.tobiasroeser.lambdatest.testng.FreeSpec;

public class AggregateShortOptionsTest extends FreeSpec {

	public static class Config {
		@CmdOption(names = { "-f", "--file" }, args = { "FILE" })
		String file = null;

		@CmdOption(names = { "-l" })
		boolean formatLong = false;

		@CmdOption(names = { "-s", "--size" })
		boolean showSize = false;
	}

	public static class Param {
		@CmdOption(args = { "PARAMETER" })
		String param;
	}

	public AggregateShortOptionsTest() {

		test("Setting all short options separate should work (reference test)", () -> {
			final Config config = new Config();
			final CmdlineParser cp = new CmdlineParser(config);
			cp.setAggregateShortOptionsWithPrefix("-");
			cp.parse(new String[] { "-l", "-s", "-f", "file.txt" });
			expectTrue(config.formatLong);
			expectTrue(config.showSize);
			expectEquals(config.file, "file.txt");
		});

		test("Aggregation is not supported when disabled", () -> {
			final Config config = new Config();
			final CmdlineParser cp = new CmdlineParser(config);
			cp.setAggregateShortOptionsWithPrefix(null);
			intercept(CmdlineParserException.class, "\\QUnsupported option or parameter found: -ls\\E", () -> {
				cp.parse(new String[] { "-ls" });
			});
		});

		test("Combining two short options without args should work", () -> {
			final Config config = new Config();
			final CmdlineParser cp = new CmdlineParser(config);
			cp.setAggregateShortOptionsWithPrefix("-");
			cp.parse(new String[] { "-ls" });
			expectTrue(config.formatLong);
			expectTrue(config.showSize);
			expectEquals(config.file, null);
		});

		test("Combining two short options with args at end should work", () -> {
			final Config config = new Config();
			final CmdlineParser cp = new CmdlineParser(config);
			cp.setAggregateShortOptionsWithPrefix("-");
			cp.parse(new String[] { "-lf", "file.txt" });
			expectTrue(config.formatLong);
			expectFalse(config.showSize);
			expectEquals(config.file, "file.txt");
		});

		test("Combining two short options with args in between should work", () -> {
			final Config config = new Config();
			final CmdlineParser cp = new CmdlineParser(config);
			cp.setAggregateShortOptionsWithPrefix("-");
			cp.parse(new String[] { "-lfs", "file.txt" });
			expectTrue(config.formatLong);
			expectTrue(config.showSize);
			expectEquals(config.file, "file.txt");
		});

		test("Comnbining short options with missing args should fail", () -> {
			final Config config = new Config();
			final CmdlineParser cp = new CmdlineParser(config);
			cp.setAggregateShortOptionsWithPrefix("-");
			intercept(CmdlineParserException.class,
					"\\QMissing argument(s): FILE. Option \"-f\" requires 1 arguments, but you gave 0.\\E", () -> {
						cp.parse(new String[] { "-lfs" });
					});
		});

		test("Comnbining unknown short options should fail", () -> {
			final Config config = new Config();
			final CmdlineParser cp = new CmdlineParser(config);
			cp.setAggregateShortOptionsWithPrefix("-");
			intercept(CmdlineParserException.class, "\\QUnsupported option or parameter found: -lsa\\E", () -> {
				// "-a" is an unknown option
				cp.parse(new String[] { "-lsa" });
			});
		});

		test("Comnbining unknown short options should result in combined options parsed as main parameter", () -> {
			final Config config = new Config();
			final Param param = new Param();
			final CmdlineParser cp = new CmdlineParser(config, param);
			cp.setAggregateShortOptionsWithPrefix("-");
			// "-a" is an unknown option
			cp.parse(new String[] { "-lsa" });
			expectFalse(config.formatLong);
			expectFalse(config.showSize);
			expectEquals(config.file, null);
			expectEquals(param.param, "-lsa");
		});

	}

}
