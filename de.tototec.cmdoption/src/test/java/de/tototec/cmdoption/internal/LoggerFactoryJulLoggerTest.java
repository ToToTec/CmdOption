package de.tototec.cmdoption.internal;

import static org.testng.Assert.assertEquals;
import de.tobiasroeser.lambdatest.testng.FreeSpec;
import de.tototec.cmdoption.internal.LoggerFactory.JavaUtilLogger;

public class LoggerFactoryJulLoggerTest extends FreeSpec {

	{
		final RuntimeException ex = new RuntimeException();

		testJulFormatter("{}", null, "{}");
		testJulFormatter("{ }", null, "{ }");
		testJulFormatter("{", null, "{");
		testJulFormatter("}", null, "}");
		testJulFormatter("{}", ex, "{}", ex);
		testJulFormatter("{ }", ex, "{ }", ex);
		testJulFormatter("{", ex, "{", ex);
		testJulFormatter("}", ex, "}", ex);
		testJulFormatter("123", null, "1{}3", "2");
		testJulFormatter("123", null, "{}{}3", "1", "2");
		testJulFormatter("123", null, "{}{}{}", "1", "2", "3");
		testJulFormatter("123", ex, "1{}3", "2", ex);
		testJulFormatter("123", ex, "{}{}3", "1", "2", ex);
		testJulFormatter("123", ex, "{}{}{}", "1", "2", "3", ex);
	}

	public void testJulFormatter(final String expectedMsg, final Throwable expectedCause, final String msg,
			final Object... args) {
		final JavaUtilLogger logger = new LoggerFactory.JavaUtilLogger(getClass());
		test("format: " + msg + " with " + FList.mkString(args, ","), () -> {
			assertEquals(
					logger.formattedMsgAndCause(msg, args),
					new Object[] { expectedMsg, expectedCause }
					);
		});
	}

}
