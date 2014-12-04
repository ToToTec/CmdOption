package de.tototec.cmdoption.internal;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public abstract class I18nFactory {

	public static I18n getI18n(final Class<?> context) {
		return getI18n(context, Locale.getDefault());
	}

	public static I18n getI18n(final Class<?> context, final Locale locale) {
		// TODO: Later, try to load a real I18n object, e.g. de.tototec.i18n
		return new I18n() {

			@Override
			public String trn(final String msgid, final String msgidPlural, final long n, final Object... params) {
				final String translated = n == 1 ? msgid : msgidPlural;
				return tr(translated, params);
			}

			@Override
			public String trcn(final String context, final String msgid, final String msgidPlural, final long n,
					final Object... params) {
				return trn(msgid, msgidPlural, n, params);
			}

			@Override
			public String trc(final String context, final String msgid, final Object... params) {
				return tr(msgid, params);
			}

			@Override
			public String tr(final String msgid, final Object... params) {
				String translated;
				try {
					translated = ResourceBundle.getBundle(context.getPackage().getName() + ".Messages", locale,
							context.getClassLoader()).getString(msgid);
				} catch (final MissingResourceException e) {
					translated = msgid;
				}
				return params == null || params.length == 0 ? translated : MessageFormat.format(translated, params);
			}

			@Override
			public PreparedI18n preparetr(final String msg, final Object... params) {
				final I18n outer = this;
				return new PreparedI18n() {

					@Override
					public String tr() {
						return outer.tr(msg, params);
					}

					@Override
					public String notr() {
						if (params != null && params.length > 0) {
							return MessageFormat.format(msg, params);
						} else {
							return msg;
						}
					}
				};
			}

			@Override
			public Locale getLocale() {
				return locale;
			}
		};
	}
}
