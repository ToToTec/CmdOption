package de.tototec.cmdoption.handler;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Apply an one-arg option to a field of type {@link String}.
 * 
 */
public class StringFieldHandler implements CmdOptionHandler {

	public boolean canHandle(final AccessibleObject element, final int argCount) {
		if (element instanceof Field && argCount == 1) {
			final Field field = (Field) element;
			return field.getType().equals(String.class);
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
			throw new CmdOptionHandlerException("Could not apply parameters: " + Arrays.toString(args) + " to field "
					+ element, e);
		}

	}

}
