package de.tototec.cmdoption.handler;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Apply an one-arg option to a {@link Integer} (or <code>int</code>) field or
 * method.
 * 
 * @since 0.3.1
 */
public class IntegerHandler implements CmdOptionHandler {

	public IntegerHandler() {
	}

	public boolean canHandle(final AccessibleObject element, final int argCount) {
		if (element instanceof Field && argCount == 1) {
			final Field field = (Field) element;
			return field.getType().equals(Integer.class) || field.getType().equals(int.class);
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
			throw new CmdOptionHandlerException("Could not read integer value '" + args[0] + "'.", e);
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
			throw new CmdOptionHandlerException("Could not apply argument '" + args[0] + "'.", e);
		}

	}
}
