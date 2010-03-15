package de.tobiasroeser.cmdoption;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

/**
 * Apply an zero-arg option to an {@link Boolean} (or <code>boolean</code>)
 * field. If the option is present, the field will be evaluated to
 * <code>true</code>.
 * 
 */
public class BooleanOptionHandler implements CmdOptionHandler {

	public void applyParams(Object config, AccessibleObject element,
			String... args) {
		try {
			Field field = (Field) element;
			field.set(config, true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean canHandle(AccessibleObject element, int argCount) {
		return argCount == 0 && element instanceof Field;
	}

}
