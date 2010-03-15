package de.tobiasroeser.cmdoption;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Apply an n-arg option to an (setter) method with n parameters of type
 * {@link String}.
 * 
 */
public class StringMethodHandler implements CmdOptionHandler {

	public boolean canHandle(AccessibleObject element, int argCount) {
		if (element instanceof Method) {
			Method method = (Method) element;
			if (method.getParameterTypes().length == argCount) {
				boolean areStrings = true;
				for (Class<?> p : method.getParameterTypes()) {
					areStrings &= String.class.isAssignableFrom(p);
				}
				return areStrings;
			}
		}
		return false;
	}

	public void applyParams(Object config, AccessibleObject element,
			String... args) {
		try {
			Method method = (Method) element;
			method.invoke(config, (Object[]) args);
		} catch (Exception e) {
			throw new CmdOptionParseException("Could not apply parameters: "
					+ Arrays.toString(args) + " to method " + element, e);
		}
	}
}
