package de.tototec.cmdoption;

import java.net.URL;

import de.tobiasroeser.lambdatest.testng.FreeSpec;

public class ModelValidationTest extends FreeSpec {

	public static class Model_UnsupportedType {
		@CmdOption(names = { "-a" }, args = { "URL" })
		URL a;
	}

	public static class Model_InconsistencMinMax {
		@CmdOption(names = { "-a" }, args = { "a" }, minCount = 1, maxCount = 0)
		int a;
	}

	public static class Model_TwoMainParams {
		@CmdOption(args = { "a" })
		int a;
		@CmdOption(args = { "b" })
		int b;
	}

	private void validate(final String testDescription, final Object... configs) {
		test(testDescription, () -> {
			intercept(CmdlineParserException.class, () -> {
				final CmdlineParser cp = new CmdlineParser(configs);
				cp.validate();
			});
		});
	}

	public ModelValidationTest() {

		validate("Validation should detect an option without a matching handler", new Model_UnsupportedType());

		validate("Validation should detect an option with inconsistent min..max count", new Model_InconsistencMinMax());

		validate("Validation should detect an option with more than one main parameter", new Model_TwoMainParams());
	}

}
