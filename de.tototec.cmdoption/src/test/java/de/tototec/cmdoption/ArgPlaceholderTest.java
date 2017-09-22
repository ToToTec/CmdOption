package de.tototec.cmdoption;

import static org.testng.Assert.assertEquals;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import de.tobiasroeser.lambdatest.testng.FreeSpec;

public class ArgPlaceholderTest extends FreeSpec {

	public static class Config7 {
		@CmdOption(names = "-a", description = "A")
		public void setA() {}

		@CmdOption(args = { "1" }, description = "B")
		public void setB(final String one) {}
	}

	public static class Config8 {
		@CmdOption(names = "-a", args = { "1" }, description = "A")
		public void setA(final String one) {}

		@CmdOption(args = { "1", "2" }, description = "B with args")
		public void setB(final String one, final String two) {}
	}

	public static class Config9 {
		@CmdOption(names = "-a", args = { "1" }, description = "A with arg {0}")
		public void setA(final String one) {}

		@CmdOption(args = { "1", "2" }, description = "B with arg {0} and {1}")
		public void setB(final String one, final String two) {}
	}

	{
		test("description placeholder without any args", () -> {
			final StringBuilder sb = new StringBuilder();
			final CmdlineParser cp = new CmdlineParser(new Config7());
			cp.setUsageFormatter(new DefaultUsageFormatter(true, 80));
			cp.usage(sb);
			assertEquals(sb.toString(), "Usage: <main class> [options] [parameter]\n\n"
					+ "Options:\n"
					+ "  -a  A\n\n"
					+ "Parameter:\n"
					+ "  1  B\n");
		});
		test("description unused placeholder with args", () -> {
			final StringBuilder sb = new StringBuilder();
			final CmdlineParser cp = new CmdlineParser(new Config8());
			cp.setUsageFormatter(new DefaultUsageFormatter(true, 80));
			cp.usage(sb);
			assertEquals(sb.toString(), "Usage: <main class> [options] [parameter]\n\n"
					+ "Options:\n"
					+ "  -a 1  A\n\n"
					+ "Parameter:\n"
					+ "  1 2  B with args\n");
		});
		test("description used placeholder with args", () -> {
			final StringBuilder sb = new StringBuilder();
			final CmdlineParser cp = new CmdlineParser(new Config9());
			cp.setUsageFormatter(new DefaultUsageFormatter(true, 80));
			cp.usage(sb);
			assertEquals(sb.toString(), "Usage: <main class> [options] [parameter]\n\n"
					+ "Options:\n"
					+ "  -a 1  A with arg 1\n\n"
					+ "Parameter:\n"
					+ "  1 2  B with arg 1 and 2\n");
		});

		test("description used placeholder with args and translation", () -> {
			final StringBuilder sb = new StringBuilder();
			final CmdlineParser cp = new CmdlineParser(new Config9());
			cp.setUsageFormatter(new DefaultUsageFormatter(true, 80));

			final ResourceBundle rb = new ResourceBundle() {
				private final Map<String, String> trs = new LinkedHashMap<String, String>() {
					private static final long serialVersionUID = 1L;
					{
						put("1", "one");
						put("2", "two");
					}
				};

				@Override
				protected Object handleGetObject(final String key) {
					return trs.get(key);
				}

				@Override
				public Enumeration<String> getKeys() {
					final Iterator<String> it = trs.keySet().iterator();
					return new Enumeration<String>() {

						@Override
						public boolean hasMoreElements() {
							return it.hasNext();
						}

						@Override
						public String nextElement() {
							return it.next();
						}

					};
				}
			};
			cp.setResourceBundle(rb);
			cp.usage(sb);
			assertEquals(sb.toString(), "Usage: <main class> [options] [parameter]\n\n"
					+ "Options:\n"
					+ "  -a one  A with arg one\n\n"
					+ "Parameter:\n"
					+ "  one two  B with arg one and two\n");
		});
	}

}
