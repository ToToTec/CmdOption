package de.tobiasroeser.cmdoption.example;

import junit.framework.Assert;

import org.testng.annotations.Test;

public class GreetingAppTest {

	@Test
	public void testHelp() {
		int code = new GreetingApp().run(new String[] { "--help" });
		Assert.assertEquals(-1, code);
	}

	@Test
	public void testGreetUnknown() {
		int code = new GreetingApp().run(new String[] {});
		Assert.assertEquals(0, code);
	}

	@Test
	public void testGreetMrT() {
		int code = new GreetingApp().run(new String[] { "--my-name", "Mr. T" });
		Assert.assertEquals(0, code);
	}

}
