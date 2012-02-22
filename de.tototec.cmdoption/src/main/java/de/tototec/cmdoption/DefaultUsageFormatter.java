package de.tototec.cmdoption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DefaultUsageFormatter implements UsageFormatter {

	private boolean withCommandOptions = false;

	public void setWithCommandOptions(boolean withCommandOptions) {
		this.withCommandOptions = withCommandOptions;
	}

	public void format(StringBuilder output, String programName, List<OptionHandle> options,
			List<CommandHandle> commands, OptionHandle parameter) {
		format(output, programName, options, commands, parameter, null, true, "");
	}

	public void format(StringBuilder output, String programName, List<OptionHandle> options,
			List<CommandHandle> commands, OptionHandle parameter, String selectedCommand, boolean withUsage,
			String prefix) {

		ArrayList<OptionHandle> sortedOptions = new ArrayList<OptionHandle>(options);
		for (Iterator<OptionHandle> it = sortedOptions.iterator(); it.hasNext();) {
			if (it.next().isHidden()) {
				it.remove();
			}
		}

		// Usage
		output.append("Usage: ");
		output.append(programName);
		if (!options.isEmpty()) {
			output.append(" [options]");
		}
		if (!commands.isEmpty()) {
			output.append(" [command] [command options]");
		}
		if (parameter != null) {
			output.append(" [parameter]");
		}
		output.append("\n");

		// Options
		if (!options.isEmpty()) {
			Collections.sort(sortedOptions, new OptionHandle.OptionHandleComparator());

			// Create columns: 1. option name + args, 2. description
			LinkedList<String[]> optionsToFormat = new LinkedList<String[]>();
			for (OptionHandle option : sortedOptions) {
				final String optionNames = Util.mkString(option.getNames(), null, ",", null);
				final String argNames = Util.mkString(option.getArgs(), null, " ", null);
				optionsToFormat.add(new String[] { optionNames + (argNames.length() == 0 ? "" : (" " + argNames)),
						option.getDescription() });
			}

			// Calc width of first column
			int firstColSize = 8;
			for (String[] strings : optionsToFormat) {
				if (strings.length > 0) {
					firstColSize = Math.max(firstColSize, strings[0].length());
				}
			}

			firstColSize += 2;
			output.append("\nOptions:\n");
			for (String[] strings : optionsToFormat) {
				output.append(strings[0]);
				for (int count = firstColSize - strings[0].length(); count > 0; --count) {
					output.append(" ");
				}
				output.append(strings[1]);
				output.append("\n");
			}
		}

		// TODO: Commands
		if (!commands.isEmpty()) {
			int firstColSize = 8;
			for (CommandHandle cmd : commands) {
				firstColSize = Math.max(firstColSize, Util.mkString(cmd.getNames(), null, ",", null).length());
			}
			firstColSize += 2;
			output.append("\nCommands:\n");
			for (CommandHandle cmd : commands) {
				String cmdName = Util.mkString(cmd.getNames(), null, ",", null);
				output.append(cmdName);
				for (int count = firstColSize - cmdName.length(); count > 0; --count) {
					output.append(" ");
				}
				output.append(cmd.getDescription());
				output.append("\n");
			}
		}

		// TODO: Parameter
		if (parameter != null) {
			output.append("\nParameter:\n");
			output.append(Util.mkString(parameter.getArgs(), null, " ", null));
			if (parameter.getDescription() != null) {
				output.append("  ");
				output.append(parameter.getDescription());
			}
			output.append("\n");
		}

	}
}
