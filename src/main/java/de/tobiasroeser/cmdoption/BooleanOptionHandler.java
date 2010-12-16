package de.tobiasroeser.cmdoption;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
			if (element instanceof Field) {
				Field field = (Field) element;
				field.set(config, true);
			} else {
				Method method = (Method) element;
				if (method.getParameterTypes().length == 1) {
					method.invoke(config, true);
				} else {
					method.invoke(config);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean canHandle(AccessibleObject element, int argCount) {
		if (argCount != 0)
			return false;

		if (element instanceof Field) {
			Field field = (Field) element;
			Class<?> type = field.getType();
			return boolean.class.equals(type) || Boolean.class.equals(type);
		} else if (element instanceof Method) {
			Method method = (Method) element;
			if (method.getParameterTypes().length == 0) {
				return true;
			}
			if (method.getParameterTypes().length == 1) {
				Class<?> type = method.getParameterTypes()[0];
				return boolean.class.equals(type) || Boolean.class.equals(type);
			}
		}
		return false;
	}
}
