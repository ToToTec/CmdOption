package de.tototec.cmdoption.handler;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.tototec.cmdoption.internal.I18n;
import de.tototec.cmdoption.internal.I18n.PreparedI18n;
import de.tototec.cmdoption.internal.I18nFactory;

/**
 * Apply an one-arg option to a {@link Boolean} (or <code>boolean</code>) field
 * or method.
 *
 * Evaluates the argument to <code>true</code> if it is "true", "on" or "1".
 *
 * You can customize the words interpreted as <code>true</code> or
 * <code>false</code> by using the non-default constructor.
 *
 * @since 0.3.0
 */
public class BooleanHandler implements CmdOptionHandler {

	private final String[] trueWords;
	private final String[] falseWords;
	private final boolean caseSensitive;

	public BooleanHandler() {
		this(new String[] { "on", "true", "1" }, new String[] { "off", "false", "0" }, false);
	}

	/**
	 * If the list of falseWords is empty or <code>null</code>, any words not in
	 * trueWords is considered as false.
	 *
	 * @param trueWords
	 * @param falseWords
	 * @param caseSensitive
	 */
	public BooleanHandler(final String[] trueWords, final String[] falseWords, final boolean caseSensitive) {
		this.trueWords = trueWords;
		this.falseWords = falseWords;
		this.caseSensitive = caseSensitive;
	}

	public boolean canHandle(final AccessibleObject element, final int argCount) {
		if (element instanceof Field && argCount == 1) {
			final Field field = (Field) element;
			return !Modifier.isFinal(field.getModifiers())
					&& (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class));
		} else if (element instanceof Method && argCount == 1) {
			final Method method = (Method) element;
			if (method.getParameterTypes().length == 1) {
				final Class<?> type = method.getParameterTypes()[0];
				return boolean.class.equals(type) || Boolean.class.equals(type);
			}
		}
		return false;
	}

	public void applyParams(final Object config, final AccessibleObject element, final String[] args,
			final String optionName) throws CmdOptionHandlerException {

		String arg = args[0];
		if (!caseSensitive) {
			arg = arg.toLowerCase();
		}

		Boolean decission = null;

		for (final String word : trueWords) {
			if (arg.equals(caseSensitive ? word : word.toLowerCase())) {
				decission = true;
				break;
			}
		}

		if (decission == null) {
			if (falseWords == null || falseWords.length == 0) {
				decission = false;
			} else {
				for (final String word : falseWords) {
					if (arg.equals(caseSensitive ? word : word.toLowerCase())) {
						decission = false;
						break;
					}
				}
			}
		}

		if (decission == null) {
			final I18n i18n = I18nFactory.getI18n(BooleanHandler.class);
			final PreparedI18n msg = i18n.preparetr("Could not parse argument \"{0}\" as boolean parameter.", args[0]);
			throw new CmdOptionHandlerException(msg.notr(), msg.tr());
		}

		try {
			if (element instanceof Field) {
				final Field field = (Field) element;
				field.set(config, decission);
			} else {
				final Method method = (Method) element;
				method.invoke(config, decission.booleanValue());
			}
		} catch (final Exception e) {
			final I18n i18n = I18nFactory.getI18n(BooleanHandler.class);
			final PreparedI18n msg = i18n.preparetr("Could not apply argument \"{0}\".", args[0]);
			throw new CmdOptionHandlerException(msg.notr(), e, msg.tr());
		}

	}
}
