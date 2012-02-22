package de.tototec.cmdoption;

import java.util.Iterator;

public abstract class Util {

	public static String mkString(final Iterable<?> iterable, final String start, final String sep, final String end) {
		final StringBuilder text = new StringBuilder();
		if (start != null)
			text.append(start);
		if (iterable != null) {
			boolean isFirst = true;
			for (final Iterator<?> it = iterable.iterator(); it.hasNext();) {
				final Object part = it.next();
				if (!isFirst && sep != null)
					text.append(sep);
				isFirst = false;
				text.append(part == null ? "null" : part.toString());
			}
		}
		if (end != null)
			text.append(end);
		return text.toString();
	}

	public static String mkString(final Object[] array, final String start, final String sep, final String end) {
		final StringBuilder text = new StringBuilder();
		if (start != null)
			text.append(start);
		if (array != null && array.length > 0) {
			boolean isFirst = true;
			for (final Object part : array) {
				if (!isFirst && sep != null)
					text.append(sep);
				isFirst = false;
				text.append(part == null ? "null" : part.toString());
			}
		}
		if (end != null)
			text.append(end);
		return text.toString();
	}
}
