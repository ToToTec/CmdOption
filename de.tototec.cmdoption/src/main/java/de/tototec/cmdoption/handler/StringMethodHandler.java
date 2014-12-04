package de.tototec.cmdoption.handler;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.Arrays;

import de.tototec.cmdoption.internal.I18n;
import de.tototec.cmdoption.internal.I18n.PreparedI18n;
import de.tototec.cmdoption.internal.I18nFactory;

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
			final I18n i18n = I18nFactory.getI18n(StringMethodHandler.class);
			final PreparedI18n msg = i18n.preparetr("Could not apply parameters: {0} to method {1}",
					Arrays.toString(args), element);
			throw new CmdOptionHandlerException(msg.notr(), e, msg.tr());
		}
	}
}
