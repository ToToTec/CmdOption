package de.tototec.cmdoption.handler;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;

import java.net.URL;

import de.tobiasroeser.lambdatest.junit.FreeSpec;
import de.tototec.cmdoption.CmdOption;
import de.tototec.cmdoption.CmdlineParser;
import de.tototec.cmdoption.CmdlineParserException;

public class UrlHandlerTest extends FreeSpec {

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

	public CmdlineParser cp() {
		final CmdlineParser cp = new CmdlineParser();
		cp.unregisterAllHandler();
		cp.registerHandler(new UrlHandler());
		return cp;
	}

	{
		test("URL field", () -> {
			final Config config = new Config();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("--url-one", "http://cmdoption.tototec.de");
			expectEquals(config.getUrlOne(), new URL("http://cmdoption.tototec.de"));
			expectEquals(config.getUrlTwo(), null);
		});

		test("URL field fail with invalid URL", () -> {
			final Config config = new Config();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			intercept(CmdlineParserException.class, "Invalid url: \"http//cmdoption.tototec.de\"", () -> {
				cp.parse("--url-one", "http//cmdoption.tototec.de");
			});
		});

		test("URL method", () -> {
			final Config config = new Config();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			cp.parse("--url-two", "http://cmdoption.tototec.de");
			expectEquals(config.getUrlTwo(), new URL("http://cmdoption.tototec.de"));
			expectEquals(config.getUrlOne(), null);
		});

		test("URL method fail with invalid URL", () -> {
			final Config config = new Config();
			final CmdlineParser cp = cp();
			cp.addObject(config);
			intercept(CmdlineParserException.class, "Invalid url: \"http//cmdoption.tototec.de\"", () -> {
				cp.parse("--url-two", "http//cmdoption.tototec.de");
			});
		});

	}

}
