package de.tobiasroeser.cmdoption.example;

import org.testng.Assert;
import org.testng.annotations.Test;

public class GreetingAppTest {

	@Test
	public void testHelp() {
		int code = new GreetingApp().run(new String[] { "--help" });
		Assert.assertEquals(code, -1);
	}

	@Test
	public void testGreetUnknown() {
		int code = new GreetingApp().run(new String[] {});
		Assert.assertEquals(code, 0);
	}

	@Test
	public void testGreetMrT() {
		int code = new GreetingApp().run(new String[] { "--my-name", "Mr. T" });
		Assert.assertEquals(code, 0);
	}

}
