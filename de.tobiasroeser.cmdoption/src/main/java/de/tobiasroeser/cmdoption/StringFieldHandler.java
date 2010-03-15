package de.tobiasroeser.cmdoption;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Apply an one-arg option to a field of type {@link String}.
 * 
 */
public class StringFieldHandler implements CmdOptionHandler {

	public boolean canHandle(AccessibleObject element, int argCount) {
		if (element instanceof Field && argCount == 1) {
			Field field = (Field) element;
			return field.getType().equals(String.class);
		}
		return false;
	}

	public void applyParams(Object config, AccessibleObject element,
			String... args) {

		try {
			Field field = (Field) element;
			field.set(config, args[0]);
		} catch (Exception e) {
			// TODO better message
			throw new CmdOptionParseException("Could not apply parameters: "
					+ Arrays.toString(args) + " to field " + element, e);
		}

	}

}
