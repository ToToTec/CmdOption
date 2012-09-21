package de.tototec.cmdoption.handler;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Apply an n-arg option to an (setter) method with n parameters of type
 * {@link String}.
 * 
 */
public class StringMethodHandler implements CmdOptionHandler {

	public boolean canHandle(final AccessibleObject element, final int argCount) {
		if (element instanceof Method) {
			final Method method = (Method) element;
			if (method.getParameterTypes().length == argCount) {
				boolean areStrings = true;
				for (final Class<?> p : method.getParameterTypes()) {
					areStrings &= String.class.isAssignableFrom(p);
				}
				return areStrings;
			}
		}
		return false;
	}

	public void applyParams(final Object config, final AccessibleObject element, final String[] args,
			final String optionName) throws CmdOptionHandlerException {
		try {
			final Method method = (Method) element;
			method.invoke(config, (Object[]) args);
		} catch (final Exception e) {
			throw new CmdOptionHandlerException("Could not apply parameters: " + Arrays.toString(args) + " to method "
					+ element, e);
		}
	}
}
