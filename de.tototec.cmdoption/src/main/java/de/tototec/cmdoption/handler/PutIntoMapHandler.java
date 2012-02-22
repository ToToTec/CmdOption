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

	public void applyParams(Object config, AccessibleObject element,
			String[] args) throws CmdOptionHandlerException {
		try {
			Field field = (Field) element;
			@SuppressWarnings("unchecked")
			Map<String, String> map = (Map<String, String>) field.get(config);
			map.put(args[0], args[1]);
		} catch (Exception e) {
			throw new CmdOptionHandlerException("Could not apply parameters: "
					+ Arrays.toString(args) + " to field " + element, e);
		}
	}

	public boolean canHandle(AccessibleObject element, int argCount) {
		return argCount == 2 && element instanceof Field
				&& Map.class.isAssignableFrom(((Field) element).getType());
	}
}
