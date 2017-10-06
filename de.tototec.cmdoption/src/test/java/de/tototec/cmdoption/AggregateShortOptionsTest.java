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
			intercept(CmdlineParserException.class, "Unsupported option or parameter found: -ls", () -> {
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


	}

}
