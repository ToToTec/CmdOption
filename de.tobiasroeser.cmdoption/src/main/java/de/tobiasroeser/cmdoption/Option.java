package de.tobiasroeser.cmdoption;

import java.lang.reflect.AccessibleObject;
import java.util.List;

/**
 * Representation of an command line option.
 * 
 * @see CmdOption
 * 
 */
public class Option implements Comparable<Option> {

	private final String longOption;
	private final String shortOption;
	private final String description;
	private final String[] args;
	private final Class<? extends CmdOptionHandler> cmdOptionHandler;
	private final AccessibleObject element;
	private final int minCount;
	private final int maxCount;

	public Option(String longOption, String shortOption, String description,
			String... args) {
		this(longOption, shortOption, description, null, null, args, 0, 1);
	}

	public Option(String longOption, String shortOption, String description,
			Class<? extends CmdOptionHandler> cmdOptionHandler,
			AccessibleObject element, String[] args, int minCount, int maxCount) {
		this.longOption = longOption;
		this.shortOption = shortOption;
		this.minCount = minCount;
		this.maxCount = maxCount;
		this.cmdOptionHandler = cmdOptionHandler != null ? cmdOptionHandler
				: CmdOptionHandler.class;
		this.element = element;
		if (longOption == null && shortOption == null) {
			throw new IllegalArgumentException(
					"Must at least give one option type (long or short).");
		}
		this.description = description;
		this.args = args;
	}

	/**
	 * Scan the string for occurrences of this option and return the position of
	 * the option, if found.
	 * 
	 * @param params
	 *            The list of parameters.
	 * @return The position of the option in the given list <code>params</code>
	 *         or <code>-1</code> if not found.
	 */
	public int scanPosition(final List<String> params) {
		int index = -1;
		if (longOption != null) {
			index = params.indexOf("--" + longOption);
		}
		if (index == -1 && shortOption != null) {
			index = params.indexOf("-" + shortOption);
		}
		return index;
	}

	public boolean match(String param) {
		if (longOption != null && param.equals("--" + longOption)) {
			return true;
		}
		if (shortOption != null && param.equals("-" + shortOption)) {
			return true;
		}
		return false;
	}

	public String getLongOption() {
		return longOption;
	}

	public String getShortOption() {
		return shortOption;
	}

	public String getDescription() {
		return description;
	}

	public String[] getArgs() {
		return args;
	}

	public int getArgCount() {
		if (args != null) {
			return args.length;
		} else {
			return 0;
		}
	}

	public int getMinCount() {
		return minCount;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public Class<? extends CmdOptionHandler> getCmdOptionHandler() {
		return cmdOptionHandler;
	}

	public AccessibleObject getElement() {
		return element;
	}

	public String formatOptionString() {

		String formatted = null;
		if (longOption != null && shortOption != null) {
			formatted = "--" + longOption + ", -" + shortOption;
		} else if (longOption != null) {
			formatted = "--" + longOption;
		} else {
			formatted = "-" + shortOption;
		}

		String arguments = null;
		if (args != null && args.length > 0) {
			for (String arg : args) {
				if (arguments == null) {
					arguments = arg;
				} else {
					arguments += " " + arg;
				}
			}
			return formatted + " " + arguments;
		} else {
			return formatted;
		}
	}

	public int compareTo(Option o) {
		String left = getLongOption() != null ? getLongOption()
				: getShortOption();
		String right = o.getLongOption() != null ? o.getLongOption() : o
				.getShortOption();

		int comp = left.toLowerCase().compareTo(right.toLowerCase());
		return comp != 0 ? comp : left.compareTo(right);
	}

	@Override
	public String toString() {
		return getLongOption() != null ? "--" + getLongOption() : "-"
				+ getShortOption();
	}

}
