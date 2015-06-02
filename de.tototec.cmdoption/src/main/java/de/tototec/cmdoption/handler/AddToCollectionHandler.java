package de.tototec.cmdoption.handler;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Add an one-arg option argument to a mutable collection of strings.
 */
public class AddToCollectionHandler implements CmdOptionHandler {

	public void applyParams(final Object config, final AccessibleObject element, final String[] args,
			final String optionName) {
		try {
			final Field field = (Field) element;
			@SuppressWarnings("unchecked")
			final Collection<String> collection = (Collection<String>) field.get(config);
			collection.add(args[0]);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean canHandle(final AccessibleObject element, final int argCount) {
		return argCount == 1 && element instanceof Field
				&& Collection.class.isAssignableFrom(((Field) element).getType());
	}
}
