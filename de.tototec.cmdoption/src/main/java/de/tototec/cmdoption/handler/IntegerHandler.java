package de.tototec.cmdoption.handler;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.tototec.cmdoption.internal.I18n;
import de.tototec.cmdoption.internal.I18n.PreparedI18n;
import de.tototec.cmdoption.internal.I18nFactory;

/**
 * Apply an one-arg option to a {@link Integer} (or <code>int</code>) field or
 * method.
 *
 * @since 0.3.1
 */
public class IntegerHandler implements CmdOptionHandler {

	public IntegerHandler() {}

	public boolean canHandle(final AccessibleObject element, final int argCount) {
		if (element instanceof Field && argCount == 1) {
			final Field field = (Field) element;
			return !Modifier.isFinal(field.getModifiers())
					&& (field.getType().equals(Integer.class) || field.getType().equals(int.class));
		} else if (element instanceof Method && argCount == 1) {
			final Method method = (Method) element;
			if (method.getParameterTypes().length == 1) {
				final Class<?> type = method.getParameterTypes()[0];
				return int.class.equals(type) || Integer.class.equals(type);
			}
		}
		return false;
	}

	public void applyParams(final Object config, final AccessibleObject element, final String[] args,
			final String optionName) throws CmdOptionHandlerException {

		int parsedValue = 0;

		try {
			final String arg = args[0];
			parsedValue = Integer.parseInt(arg);
		} catch (final NumberFormatException e) {
			final I18n i18n = I18nFactory.getI18n(IntegerHandler.class);
			final PreparedI18n msg = i18n.preparetr("Could not read integer value \"{0}\".", args[0]);
			throw new CmdOptionHandlerException(msg.notr(), e, msg.tr());
		}

		try {
			if (element instanceof Field) {
				final Field field = (Field) element;
				field.set(config, parsedValue);
			} else {
				final Method method = (Method) element;
				method.invoke(config, parsedValue);
			}
		} catch (final Exception e) {
			final I18n i18n = I18nFactory.getI18n(IntegerHandler.class);
			final PreparedI18n msg = i18n.preparetr("Could not apply argument \"{0}\".", args[0]);
			throw new CmdOptionHandlerException(msg.notr(), e, msg.tr());
		}

	}
}
