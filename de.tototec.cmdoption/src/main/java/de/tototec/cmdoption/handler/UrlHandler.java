package de.tototec.cmdoption.handler;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import de.tototec.cmdoption.internal.I18n;
import de.tototec.cmdoption.internal.I18n.PreparedI18n;
import de.tototec.cmdoption.internal.I18nFactory;

public class UrlHandler implements CmdOptionHandler {

	public boolean canHandle(final AccessibleObject element, final int argCount) {
		if (argCount == 1) {
			if (element instanceof Field) {
				final Field field = (Field) element;
				return !Modifier.isFinal(field.getModifiers()) && field.getType().equals(URL.class);
			} else if (element instanceof Method) {
				final Class<?>[] params = ((Method) element).getParameterTypes();
				return params.length == 1 && params[0].equals(URL.class);
			}
		}
		return false;
	}

	public void applyParams(final Object config, final AccessibleObject element, final String[] args,
			final String optionName) throws CmdOptionHandlerException {
		try {
			final URL url = new URL(args[0]);
			if (element instanceof Field) {
				((Field) element).set(config, url);
			} else if (element instanceof Method) {
				((Method) element).invoke(config, url);
			}
		} catch (final MalformedURLException e) {
			final I18n i18n = I18nFactory.getI18n(UrlHandler.class);
			final PreparedI18n msg = i18n.preparetr("Invalid url: \"{0}\"", args[0]);
			throw new CmdOptionHandlerException(msg.notr(), e, msg.tr());
		} catch (final Exception e) {
			// TODO better message
			final I18n i18n = I18nFactory.getI18n(UrlHandler.class);
			final PreparedI18n msg = i18n.preparetr("Could not apply parameters: {0} to field/method {1}",
					Arrays.toString(args), element);
			throw new CmdOptionHandlerException(msg.notr(), e, msg.tr());
		}

	}
}
