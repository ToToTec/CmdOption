package de.tototec.cmdoption.handler;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class UrlHandler implements CmdOptionHandler {

	public boolean canHandle(final AccessibleObject element, final int argCount) {
		if (argCount == 1) {
			if (element instanceof Field) {
				return ((Field) element).getType().equals(URL.class);
			} else if (element instanceof Method) {
				final Class<?>[] params = ((Method) element).getParameterTypes();
				return params.length == 1 && params[0].equals(URL.class);
			}
		}
		return false;
	}

	public void applyParams(final Object config, final AccessibleObject element, final String[] args,
			final String optionName) throws CmdOptionHandlerException {
		try {
			final URL url = new URL(args[0]);
			if (element instanceof Field) {
				((Field) element).set(config, url);
			} else if (element instanceof Method) {
				((Method) element).invoke(config, url);
			}
		} catch (final MalformedURLException e) {
			throw new CmdOptionHandlerException("Invalid url: \"" + args[0] + "\"", e);
		} catch (final Exception e) {
			// TODO better message
			throw new CmdOptionHandlerException("Could not apply parameters: " + Arrays.toString(args)
					+ " to field/method " + element, e);
		}

	}
}
