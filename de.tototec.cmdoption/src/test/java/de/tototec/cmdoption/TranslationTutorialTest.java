package de.tototec.cmdoption;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.tobiasroeser.lambdatest.junit.FreeSpec;

public class TranslationTutorialTest extends FreeSpec {

	public TranslationTutorialTest() {

		section("Translation tutorial main should work with locales", () -> {
			final List<Locale> localesToTest = Arrays.asList(Locale.ROOT, Locale.GERMANY);

			for (final Locale locale : localesToTest) {
				test("Locale: " + locale.toString(), () -> {
					final Locale defaultLocale = Locale.getDefault();
					try {
						Locale.setDefault(locale);
						TranslationTutorialMain.main(new String[] { "--help" });
					} finally {
						Locale.setDefault(defaultLocale);
					}
				});
			}
		});
	}
}
