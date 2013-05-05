package de.tototec.cmdoption;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DelegateTest {

	class ConfigA {
		@CmdOption(names = "-a")
		private boolean a;
	}

	class ConfigB {
		@CmdOptionDelegate
		private ConfigA a = new ConfigA();
	}

	@Test
	public void testParseOption() {
		final ConfigA config = new ConfigA();
		Assert.assertFalse(config.a);

		final CmdlineParser cp = new CmdlineParser(config);
		cp.parse("-a");

		Assert.assertTrue(config.a);
	}

	@Test
	public void testParseDelegateOption() {
		final ConfigB config = new ConfigB();
		Assert.assertFalse(config.a.a);

		final CmdlineParser cp = new CmdlineParser(config);
		cp.parse("-a");

		Assert.assertTrue(config.a.a);
	}

}
