package de.tototec.cmdoption.internal;

import java.util.Locale;

public abstract class I18n {

	public static String marktr(final String string) {
		// Placeholder to allow mark of translatable strings.
		return string;
	}

	public static String marktrc(final String context, final String msgid) {
		// Placeholder to allow mark of translatable strings.
		return msgid;
	}

	public interface PreparedI18n {
		String notr();

		String tr();
	}

	public abstract String tr(final String msg, final Object... params);

	public abstract String trn(String msgid, String msgidPlural, long n, Object... params);

	public abstract String trc(String context, String msgid, Object... params);

	public abstract String trcn(String context, String msgid, String msgidPlural, long n, Object... params);

	public abstract PreparedI18n preparetr(final String msg, final Object... params);

	public abstract Locale getLocale();

}
