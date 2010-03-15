package de.tobiasroeser.cmdoption;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Add an one-arg option argument to a collection of strings.
 */
public class AddToCollectionHandler implements CmdOptionHandler {

	public void applyParams(Object config, AccessibleObject element,
			String... args) {
		try {
			Field field = (Field) element;
			@SuppressWarnings("unchecked")
			Collection<String> collection = (Collection<String>) field
					.get(config);
			collection.add(args[0]);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean canHandle(AccessibleObject element, int argCount) {
		return argCount == 1
				&& element instanceof Field
				&& Collection.class.isAssignableFrom(((Field) element)
						.getType());
	}
}
