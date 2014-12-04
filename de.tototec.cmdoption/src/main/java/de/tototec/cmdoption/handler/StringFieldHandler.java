package de.tototec.cmdoption.handler;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import de.tototec.cmdoption.internal.I18n;
import de.tototec.cmdoption.internal.I18n.PreparedI18n;
import de.tototec.cmdoption.internal.I18nFactory;

/**
 * Apply an one-arg option to a field of type {@link String}.
 *
 */
public class StringFieldHandler implements CmdOptionHandler {

	public boolean canHandle(final AccessibleObject element, final int argCount) {
		if (element instanceof Field && argCount == 1) {
			final Field field = (Field) element;
			return !Modifier.isFinal(field.getModifiers()) && field.getType().equals(String.class);
		}
		return false;
	}

	public void applyParams(final Object config, final AccessibleObject element, final String[] args,
			final String optionName) throws CmdOptionHandlerException {

		try {
			final Field field = (Field) element;
			field.set(config, args[0]);
		} catch (final Exception e) {
			// TODO better message
			final I18n i18n = I18nFactory.getI18n(StringFieldHandler.class);
			final PreparedI18n msg = i18n.preparetr("Could not apply parameters: {0} to field {1}",
					Arrays.toString(args), element);
			throw new CmdOptionHandlerException(msg.notr(), e, msg.tr());
		}

	}

}
