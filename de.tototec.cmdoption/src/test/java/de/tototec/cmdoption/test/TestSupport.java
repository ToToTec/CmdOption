package de.tototec.cmdoption.test;

import java.util.Locale;

public class TestSupport {

	public static void withLocale(final Locale locale, final Runnable r) {
		final Locale defaultLocale = Locale.getDefault();
		try {
			Locale.setDefault(Locale.ROOT);
			r.run();
		} finally {
			Locale.setDefault(defaultLocale);
		}
	}

}
