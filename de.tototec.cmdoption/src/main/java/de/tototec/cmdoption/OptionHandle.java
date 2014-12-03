package de.tototec.cmdoption;

import java.lang.reflect.AccessibleObject;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tototec.cmdoption.handler.CmdOptionHandler;
import de.tototec.cmdoption.internal.FList;

public class OptionHandle {

	private final String[] names;
	private final String description;
	private final CmdOptionHandler cmdOptionHandler;
	private final AccessibleObject element;
	private final String[] args;
	private final int minCount;
	private final int maxCount;
	private final boolean help;
	private final Object object;
	private final boolean hidden;
	private String[] requires;
	private String[] conflictsWith;

	public OptionHandle(
			final String[] names,
			final String description,
			final CmdOptionHandler cmdOptionHandler,
			final Object object,
			final AccessibleObject element,
			final String[] args,
			final int minCount,
			final int maxCount,
			final boolean help,
			final boolean hidden,
			final String[] requires,
			final String[] conflictsWith) {
		this.names = names;
		this.description = description;
		this.cmdOptionHandler = cmdOptionHandler;
		this.object = object;
		this.element = element;
		this.args = args;
		this.minCount = minCount;
		this.maxCount = maxCount;
		this.help = help;
		this.hidden = hidden;
		this.requires = requires;
		this.conflictsWith = conflictsWith;
	}

	public String[] getNames() {
		return names;
	}

	public String getDescription() {
		return description;
	}

	public CmdOptionHandler getCmdOptionHandler() {
		return cmdOptionHandler;
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

	public String[] getRequires() {
		return requires;
	}

	public String[] getConflictsWith() {
		return conflictsWith;
	}

	public static class OptionHandleComparator implements Comparator<OptionHandle> {
		public int compare(final OptionHandle o1, final OptionHandle o2) {
			// TODO: check for null and zero names
			return sanitizeString(o1.getNames()[0]).compareTo(sanitizeString(o2.getNames()[0]));
		}

		public String sanitizeString(final String string) {
			final Pattern pattern = Pattern.compile("^[^A-Za-z0-9]*(.*)$");
			final Matcher matcher = pattern.matcher(string);
			if (matcher.matches()) {
				return matcher.group(1);
			}
			return string;
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +
				"(names=" + FList.mkString(getNames(), ",") +
				",args=" + FList.mkString(getArgs(), ",") +
				",minCount=" + getMinCount() +
				",maxCount=" + getMaxCount() +
				",description=" + getDescription() +
				",requires=" + FList.mkString(getRequires(), ",") +
				",conflictsWith=" + FList.mkString(getConflictsWith(), ",") +
				",isHidden=" + isHidden() +
				",isHelp=" + isHelp() +
				",handler=" + getCmdOptionHandler().getClass().getName() +
				")";
	}

}
