package de.tototec.cmdoption.handler;

import java.net.MalformedURLException;
import java.net.URL;

import org.testng.Assert;
import org.testng.annotations.Test;

import de.tototec.cmdoption.CmdOption;
import de.tototec.cmdoption.CmdlineParser;
import de.tototec.cmdoption.CmdlineParserException;

public class UrlHandlerTest {

	public static class Config {
		@CmdOption(names = "--url-one", args = "URL")
		private URL urlOne;

		public URL getUrlOne() {
			return urlOne;
		}

		private URL _urlTwo;

		@CmdOption(names = "--url-two", args = "URL")
		public void setUrlTwo(final URL urlTwo) {
			_urlTwo = urlTwo;
		}

		public URL getUrlTwo() {
			return _urlTwo;
		}
	}

	@Test
	public void testUrlField() {
		final Config config = new Config();
		final CmdlineParser cp = new CmdlineParser();
		cp.unregisterAllHandler();
		cp.registerHandler(new UrlHandler());
		cp.addObject(config);
		cp.parse("--url-one", "http://cmdoption.tototec.de");

		final URL testUrl;
		try {
			testUrl = new URL("http://cmdoption.tototec.de");
		} catch (final MalformedURLException e1) {
			throw new RuntimeException("URL to test is invalid");
		}

		Assert.assertEquals(config.getUrlOne(), testUrl);
		Assert.assertEquals(config.getUrlTwo(), null);
	}

	@Test(expectedExceptions = CmdlineParserException.class)
	public void testUrlFieldFail() {
		final Config config = new Config();
		final CmdlineParser cp = new CmdlineParser();
		cp.unregisterAllHandler();
		cp.registerHandler(new UrlHandler());
		cp.addObject(config);
		cp.parse("--url-one", "http//cmdoption.tototec.de");
	}

	@Test
	public void testUrlMethod() {
		final Config config = new Config();
		final CmdlineParser cp = new CmdlineParser();
		cp.unregisterAllHandler();
		cp.registerHandler(new UrlHandler());
		cp.addObject(config);
		cp.parse("--url-two", "http://cmdoption.tototec.de");

		final URL testUrl;
		try {
			testUrl = new URL("http://cmdoption.tototec.de");
		} catch (final MalformedURLException e1) {
			throw new RuntimeException("URL to test is invalid");
		}

		Assert.assertEquals(config.getUrlTwo(), testUrl);
		Assert.assertEquals(config.getUrlOne(), null);
	}

	@Test(expectedExceptions = CmdlineParserException.class)
	public void testUrlMethodFail() {
		final Config config = new Config();
		final CmdlineParser cp = new CmdlineParser();
		cp.unregisterAllHandler();
		cp.registerHandler(new UrlHandler());
		cp.addObject(config);
		cp.parse("--url-two", "http//cmdoption.tototec.de");
	}

}
