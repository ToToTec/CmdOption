package de.tototec.cmdoption;

import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tototec.cmdoption.internal.FList;

public class CommandHandle {

	private final String[] names;
	private final String description;
	private final CmdlineParser cmdlineParser;
	private final Object object;
	private final boolean hidden;

	public CommandHandle(final String[] names, final String description, final CmdlineParser cmdlineParser, final Object object, final boolean hidden) {
		this.names = names;
		this.description = description;
		this.cmdlineParser = cmdlineParser;
		this.object = object;
		this.hidden = hidden;
	}

	public String[] getNames() {
		return names;
	}

	public String getDescription() {
		return description;
	}

	public CmdlineParser getCmdlineParser() {
		return cmdlineParser;
	}

	public CmdlineModel getCommandCmdlineModel() {
		return cmdlineParser.getCmdlineModel();
	}

	public Object getObject() {
		return object;
	}

	public boolean isHidden() {
		return hidden;
	}

	public static class CommandHandleComparator implements Comparator<CommandHandle> {
		public int compare(final CommandHandle c1, final CommandHandle c2) {
			// TODO: check for null and zero names
			return sanitizeString(c1.getNames()[0]).compareTo(sanitizeString(c2.getNames()[0]));
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
				"(names=" + FList.mkString(names, ",") +
				",description=" + description +
				",cmdlineParser=" + cmdlineParser +
				",object=" + object +
				",hidden=" + hidden +
				")";
	}
}
