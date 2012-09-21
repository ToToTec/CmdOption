package de.tototec.cmdoption.handler;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

/**
 * Apply an two-arg option to an {@link Map}.
 * 
 */
public class PutIntoMapHandler implements CmdOptionHandler {

	public void applyParams(final Object config, final AccessibleObject element, final String[] args,
			final String optionName) throws CmdOptionHandlerException {
		try {
			final Field field = (Field) element;
			@SuppressWarnings("unchecked")
			final Map<String, String> map = (Map<String, String>) field.get(config);
			map.put(args[0], args[1]);
		} catch (final Exception e) {
			throw new CmdOptionHandlerException("Could not apply parameters: " + Arrays.toString(args) + " to field "
					+ element, e);
		}
	}

	public boolean canHandle(final AccessibleObject element, final int argCount) {
		return argCount == 2 && element instanceof Field && Map.class.isAssignableFrom(((Field) element).getType());
	}
}
