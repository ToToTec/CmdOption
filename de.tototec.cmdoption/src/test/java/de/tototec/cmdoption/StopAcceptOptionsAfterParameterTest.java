package de.tototec.cmdoption;

import static de.tobiasroeser.lambdatest.Expect.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.tobiasroeser.lambdatest.junit.FreeSpec;

public class StopAcceptOptionsAfterParameterTest extends FreeSpec {

	public static class Config {
		@CmdOption(names = {"-i"})
		boolean argi = false;

		@CmdOption(names = {"-v"})
		boolean argv = false;

		@CmdOption(args = {"param"}, maxCount = -1)
		List<String> params = new LinkedList<>();
	}

	public StopAcceptOptionsAfterParameterTest() throws NoSuchMethodException {

		final Method featureMethod = CmdlineParser.class.getMethod("setStopAcceptOptionsAfterParameterIsSet", boolean.class);

		section("Feature: " + featureMethod.toGenericString(), () -> {

			test("should be disabled by default", () -> {
				final Config config = new Config();
				final CmdlineParser cp = new CmdlineParser(config);
				cp.parse("-i", "param", "-v", "p2");
				expectTrue(config.argi);
				expectTrue(config.argv);
				expectEquals(config.params, Arrays.asList("param", "p2"));
			});

			section("when enabled", () -> {

				test("should parse options following the first parameter as params", () -> {
					final Config config = new Config();
					final CmdlineParser cp = new CmdlineParser(config);
					cp.setStopAcceptOptionsAfterParameterIsSet(true);
					cp.parse("-i", "param", "-v", "p2");
					expectTrue(config.argi);
					expectFalse(config.argv);
					expectEquals(config.params, Arrays.asList("param", "-v", "p2"));
				});

			});


		});

	}

}
