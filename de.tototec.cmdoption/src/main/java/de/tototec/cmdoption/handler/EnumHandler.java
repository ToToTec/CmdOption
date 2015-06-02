package de.tototec.cmdoption.handler;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.EnumSet;

import de.tototec.cmdoption.internal.FList;
import de.tototec.cmdoption.internal.I18n;
import de.tototec.cmdoption.internal.I18n.PreparedI18n;
import de.tototec.cmdoption.internal.I18nFactory;

/**
 * Parse a Sting to a {@link Enum} of the expected type and applies it to a field or a
 * one-arg method. The <code>Enum.valueOf</code> method is used.
 *
 */
public class EnumHandler implements CmdOptionHandler {

	public boolean canHandle(final AccessibleObject element, final int argCount) {
		if (argCount == 1) {
			if (element instanceof Field) {
				final Field field = (Field) element;
				return !Modifier.isFinal(field.getModifiers()) && field.getType().isEnum();
			} else if (element instanceof Method) {
				final Class<?>[] params = ((Method) element).getParameterTypes();
				return params.length == 1 && params[0].isEnum();
			}
		}
		return false;
	}

	public void applyParams(final Object config, final AccessibleObject element, final String[] args,
			final String optionName) throws CmdOptionHandlerException {
		try {
			if (element instanceof Field) {
				final Field field = (Field) element;
				final Class<? extends Enum> type = (Class<? extends Enum>) field.getType();
				final Enum<?> value = Enum.valueOf(type, args[0]);
				field.set(config, value);
			} else if (element instanceof Method) {
				final Method method = (Method) element;
				final Class<? extends Enum> type = (Class<? extends Enum>) method.getParameterTypes()[0];
				final Enum<?> value = Enum.valueOf(type, args[0]);
				method.invoke(config, value);
			}
		} catch (final IllegalArgumentException e) {
			final Class<? extends Enum> type;
			if (element instanceof Field) {
				type = (Class<? extends Enum>) ((Field) element).getType();
			} else if (element instanceof Method) {
				type = (Class<? extends Enum>) ((Method) element).getParameterTypes()[0];
			} else {
				type = Enum.class;
			}
			final I18n i18n = I18nFactory.getI18n(EnumHandler.class);
			final PreparedI18n msg = i18n.preparetr("Invalid enum value: \"{0}\". Supported values: {1}", args[0],
					FList.mkString(EnumSet.allOf(type), ", "));
			throw new CmdOptionHandlerException(msg.notr(), e, msg.tr());
		} catch (final Exception e) {
			// TODO better message
			final I18n i18n = I18nFactory.getI18n(EnumHandler.class);
			final PreparedI18n msg = i18n.preparetr("Could not apply parameters: {0} to field/method {1}",
					Arrays.toString(args), element);
			throw new CmdOptionHandlerException(msg.notr(), e, msg.tr());
		}
	}

}
