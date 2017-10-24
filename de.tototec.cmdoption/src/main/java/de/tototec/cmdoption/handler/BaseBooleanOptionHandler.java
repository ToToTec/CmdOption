package de.tototec.cmdoption.handler;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Apply an zero-arg option to an {@link Boolean} (or <code>boolean</code>)
 * field. If the option is present, the field will be evaluated to either
 * <code>true</code> or <code>false</code>, depending on the constructor
 * argument <code>valueOnSet</code>.
 *
 * @since 0.5.1
 */
public class BaseBooleanOptionHandler implements CmdOptionHandler {

	private boolean valueOnSet;

	public BaseBooleanOptionHandler(final boolean valueOnSet) {
		this.valueOnSet = valueOnSet;
	}

	public void applyParams(final Object config, final AccessibleObject element, final String[] args,
			final String optionName) {
		try {
			if (element instanceof Field) {
				final Field field = (Field) element;
				field.set(config, valueOnSet);
			} else {
				final Method method = (Method) element;
				if (method.getParameterTypes().length == 1) {
					method.invoke(config, valueOnSet);
				} else {
					method.invoke(config);
				}
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean canHandle(final AccessibleObject element, final int argCount) {
		if (argCount != 0)
			return false;

		if (element instanceof Field) {
			final Field field = (Field) element;
			final Class<?> type = field.getType();
			return !Modifier.isFinal(field.getModifiers())
					&& (boolean.class.equals(type) || Boolean.class.equals(type));
		} else if (element instanceof Method) {
			final Method method = (Method) element;
			if (method.getParameterTypes().length == 0) {
				return true;
			}
			if (method.getParameterTypes().length == 1) {
				final Class<?> type = method.getParameterTypes()[0];
				return boolean.class.equals(type) || Boolean.class.equals(type);
			}
		}
		return false;
	}
}
