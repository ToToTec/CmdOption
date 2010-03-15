package de.tobiasroeser.cmdoption;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

/**
 * Apply an one-arg option to a {@link Boolean} (or <code>boolean</code>) field.
 * Evaluated the argument to <code>true</code> if it is "true", "on" or "1".
 * 
 */
public class BooleanFieldHandler implements CmdOptionHandler {

	public boolean canHandle(AccessibleObject element, int argCount) {
		if (element instanceof Field && argCount == 1) {
			Field field = (Field) element;
			return field.getType().equals(Boolean.class)
					|| field.getType().equals(boolean.class);
		}
		return false;
	}

	public void applyParams(Object config, AccessibleObject element,
			String... args) {

		try {
			Field field = (Field) element;
			String arg = args[0].toLowerCase().replaceFirst("on",
					Boolean.TRUE.toString()).replaceFirst("1",
					Boolean.TRUE.toString());
			field.set(config, Boolean.parseBoolean(arg));
		} catch (Exception e) {
			// TODO better message
			throw new RuntimeException("Could not apply parameters.", e);
		}

	}

}
