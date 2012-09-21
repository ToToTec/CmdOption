package de.tototec.cmdoption;

import java.lang.reflect.AccessibleObject;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tototec.cmdoption.handler.CmdOptionHandler;

class OptionHandle {

	private final String[] names;
	private final String description;
	private final Class<? extends CmdOptionHandler> cmdOptionHandlerType;
	private final AccessibleObject element;
	private final String[] args;
	private final int minCount;
	private final int maxCount;
	private final boolean help;
	private final Object object;
	private final boolean hidden;

	public OptionHandle(String[] names, String description, Class<? extends CmdOptionHandler> cmdOptionHandlerType,
			Object object, AccessibleObject element, String[] args, int minCount, int maxCount, boolean help,
			boolean hidden) {
		this.names = names;
		this.description = description;
		this.cmdOptionHandlerType = cmdOptionHandlerType;
		this.object = object;
		this.element = element;
		this.args = args;
		this.minCount = minCount;
		this.maxCount = maxCount;
		this.help = help;
		this.hidden = hidden;
	}

	public String[] getNames() {
		return names;
	}

	public String getDescription() {
		return description;
	}

	public Class<? extends CmdOptionHandler> getCmdOptionHandlerType() {
		return cmdOptionHandlerType;
	}

	public String[] getArgs() {
		return args;
	}

	public int getArgsCount() {
		return args == null ? 0 : args.length;
	}

	public int getMinCount() {
		return minCount;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public Object getObject() {
		return object;
	}

	public AccessibleObject getElement() {
		return element;
	}

	public boolean isHidden() {
		return hidden;
	}

	public boolean isHelp() {
		return help;
	}

	public static class OptionHandleComparator implements Comparator<OptionHandle> {
		public int compare(OptionHandle o1, OptionHandle o2) {
			// TODO: check for null and zero names
			return sanitizeString(o1.getNames()[0]).compareTo(sanitizeString(o2.getNames()[0]));
		}

		public String sanitizeString(String string) {
			Pattern pattern = Pattern.compile("^[^A-Za-z0-9]*(.*)$");
			Matcher matcher = pattern.matcher(string);
			if (matcher.matches()) {
				return matcher.group(1);
			}
			return string;
		}
	}

	public String toString() {
		return getClass().getSimpleName() + "(names=" + Util.mkString(getNames(), null, ", ", null) + ")";
	}
}