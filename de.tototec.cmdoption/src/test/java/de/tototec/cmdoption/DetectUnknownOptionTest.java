package de.tototec.cmdoption;

import de.tobiasroeser.lambdatest.junit.FreeSpec;

public class DetectUnknownOptionTest extends FreeSpec {

	public static class Config {
		@CmdOption(names = { "-o", "--option" })
		Boolean option;
	}

	public static class ConfigWithParam {
		@CmdOption(names = { "-o", "--option" })
		Boolean option;

		@CmdOption(args = { "PARAM" }, maxCount = -1)
		String param;
	}

	public DetectUnknownOptionTest() {

		section("Without detection of unsupported options by prefix", () -> {

			test("An unsupported short option should be parsed as parameter", () -> {
				final CmdlineParser cp = new CmdlineParser(new ConfigWithParam());
				cp.parse(new String[] { "-o", "-u", "param" });
			});
			test("An unsupported long option should be parsed as parameter", () -> {
				final CmdlineParser cp = new CmdlineParser(new ConfigWithParam());
				cp.parse(new String[] { "--option", "--unknown", "param" });
			});
			test("An unsupported option should fail when no params are supported", () -> {
				final CmdlineParser cp = new CmdlineParser(new Config());
				intercept(CmdlineParserException.class, "\\QUnsupported option or parameter found: -u\\E", () -> {
					cp.parse(new String[] { "-o", "-u" });
				});
			});
		});

		section("With detection of unsupported options by prefix", () -> {
			test("An unsupported short option should result in parse exception", () -> {
				final CmdlineParser cp = new CmdlineParser(new ConfigWithParam());
				cp.setOptionPrefixes("-", "--");
				intercept(CmdlineParserException.class, "\\QUnsupported option found: -u\\E", () -> {
					cp.parse(new String[] { "-o", "-u", "param" });
				});
			});
			test("An unsupported long option should result in parse exception", () -> {
				final CmdlineParser cp = new CmdlineParser(new ConfigWithParam());
				cp.setOptionPrefixes("-", "--");
				intercept(CmdlineParserException.class, "\\QUnsupported option found: --unknown\\E", () -> {
					cp.parse(new String[] { "--option", "--unknown", "param" });
				});

			});
			test("An unsupported option should result in parse exception when no params are supported", () -> {
				final CmdlineParser cp = new CmdlineParser(new Config());
				cp.setOptionPrefixes("-", "--");
				intercept(CmdlineParserException.class, "\\QUnsupported option found: -u\\E", () -> {
					cp.parse(new String[] { "-o", "-u" });
				});
			});
		});
	}

}
