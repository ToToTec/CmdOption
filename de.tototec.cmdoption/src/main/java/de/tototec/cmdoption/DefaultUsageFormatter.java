package de.tototec.cmdoption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DefaultUsageFormatter implements UsageFormatter {

	private final boolean withCommandDetails;

	private int lineLength = 80;
	private int colSpace = 2;
	private int col1Prefix = 2;

	public DefaultUsageFormatter(final boolean withCommandDetails) {
		this.withCommandDetails = withCommandDetails;
	}

	public void format(final StringBuilder output, final CmdlineModel cmdlineModel) {

		final ArrayList<OptionHandle> sortedOptions = new ArrayList<OptionHandle>(cmdlineModel.getOptions());
		for (final Iterator<OptionHandle> it = sortedOptions.iterator(); it.hasNext();) {
			if (it.next().isHidden()) {
				it.remove();
			}
		}
		Collections.sort(sortedOptions, new OptionHandle.OptionHandleComparator());

		final ArrayList<CommandHandle> sortedCommands = new ArrayList<CommandHandle>(cmdlineModel.getCommands());
		for (final Iterator<CommandHandle> it = sortedCommands.iterator(); it.hasNext();) {
			if (it.next().isHidden()) {
				it.remove();
			}
		}
		Collections.sort(sortedCommands, new CommandHandle.CommandHandleComparator());

		// About
		if (cmdlineModel.getAboutLine() != null && cmdlineModel.getAboutLine().length() > 0) {
			output.append(cmdlineModel.getAboutLine()).append("\n\n");
		}

		// Usage
		output.append("Usage: ");
		output.append(cmdlineModel.getProgramName() == null ? "program" : cmdlineModel.getProgramName());
		if (!sortedOptions.isEmpty()) {
			output.append(" [options]");
		}
		if (cmdlineModel.getParameter() != null) {
			output.append(" [parameter]");
		}
		if (!sortedCommands.isEmpty()) {
			output.append(" [command]");
			boolean cmdsHaveOptions = false;
			boolean cmdsHaveParameter = false;
			for (final CommandHandle cmd : sortedCommands) {
				cmdsHaveOptions |= !cmd.getCmdlineParser().getCmdlineModel().getOptions().isEmpty();
				cmdsHaveParameter |= cmd.getCmdlineParser().getCmdlineModel().getParameter() != null;
			}
			if (cmdsHaveOptions) {
				output.append(" [command options]");
			}
			if (cmdsHaveParameter) {
				output.append(" [command parameters]");
			}
		}
		output.append("\n");

		formatOptions(output, sortedOptions, "\nOptions:");

		formatCommands(output, sortedCommands, "\nCommands:");

		if (withCommandDetails) {
			for (final CommandHandle command : sortedCommands) {
				final ArrayList<OptionHandle> commandOptions = new ArrayList<OptionHandle>(command.getCmdlineParser()
						.getCmdlineModel().getOptions());
				for (final Iterator<OptionHandle> it = commandOptions.iterator(); it.hasNext();) {
					if (it.next().isHidden()) {
						it.remove();
					}
				}
				Collections.sort(commandOptions, new OptionHandle.OptionHandleComparator());

				formatOptions(output, commandOptions,
						"\nOptions for command: " + Util.mkString(command.getNames(), null, ", ", null));

				formatParameter(output, command.getCmdlineParser().getCmdlineModel().getParameter(),
						"\nParameter for command: " + Util.mkString(command.getNames(), null, ", ", null));
			}
		}

		// Parameter
		formatParameter(output, cmdlineModel.getParameter(), "\nParameter:");
	}

	protected void formatParameter(final StringBuilder output, final OptionHandle parameter, final String title) {
		if (parameter == null) {
			return;
		}

		output.append(title).append("\n");
		mkSpace(output, col1Prefix);
		output.append(Util.mkString(parameter.getArgs(), null, " ", null));
		if (parameter.getDescription() != null) {
			mkSpace(output, colSpace);
			output.append(parameter.getDescription());
		}
		output.append("\n");
	}

	protected void formatOptions(final StringBuilder output, final List<OptionHandle> options, final String title) {
		if (options == null || options.isEmpty()) {
			return;
		}

		final LinkedList<String[]> optionsToFormat = new LinkedList<String[]>();
		boolean hasOptions = false;
		for (final OptionHandle option : options) {
			if (option.isHidden()) {
				continue;
			}
			hasOptions = true;
			final String optionNames = Util.mkString(option.getNames(), null, ",", null);
			final String argNames = Util.mkString(option.getArgs(), null, " ", null);
			optionsToFormat.add(new String[] { optionNames + (argNames.length() == 0 ? "" : (" " + argNames)),
					option.getDescription() });
		}

		if (!hasOptions) {
			return;
		}

		if (title != null) {
			output.append(title).append("\n");
		}

		formatTable(output, optionsToFormat, col1Prefix, colSpace, lineLength);
	}

	protected void formatCommands(final StringBuilder output, final List<CommandHandle> commands, final String title) {
		if (commands == null || commands.isEmpty()) {
			return;
		}

		if (title != null) {
			output.append(title).append("\n");
		}

		final LinkedList<String[]> commandsToFormat = new LinkedList<String[]>();
		for (final CommandHandle option : commands) {
			final String commandNames = Util.mkString(option.getNames(), null, ",", null);
			commandsToFormat.add(new String[] { commandNames, option.getDescription() });
		}

		formatTable(output, commandsToFormat, col1Prefix, colSpace, lineLength);
	}

	public static void mkSpace(final StringBuilder output, final int space) {
		for (int i = 0; i < space; ++i) {
			output.append(" ");
		}
	}

	public static void formatTable(final StringBuilder output, final List<String[]> twoColData, final int prefix,
			final int space, final int maxLineLength) {
		// Calc first col width
		int firstColSize = 2;
		for (final String[] col : twoColData) {
			if (col.length > 0 && col[0] != null) {
				firstColSize = Math.max(firstColSize, col[0].length());
			}
		}

		final boolean secondColInNewLine = ((prefix + space + firstColSize + 10) > maxLineLength);

		// Write output
		for (final String[] col : twoColData) {
			if (col.length > 0) {
				// first col
				mkSpace(output, prefix);
				int cursor = prefix;

				if (col[0] != null) {
					output.append(col[0]);
					cursor += col[0].length();
				}

				// fill space to next col
				if (secondColInNewLine) {
					output.append("\n");
				} else {
					mkSpace(output, prefix + firstColSize + space - cursor);
				}

				// second col
				if (col[1] != null) {
					if (secondColInNewLine) {
						wrap(output, col[1], prefix + space, maxLineLength - prefix - space);
					} else {
						wrap(output, col[1], prefix + space + firstColSize, maxLineLength - prefix - space
								- firstColSize);
					}
				}
			}
			output.append("\n");
		}

	}

	public static void wrap(final StringBuilder output, String text, final int nextLinePrefix, final int lineLength) {
		text = text.trim();

		if (text.length() <= lineLength) {
			output.append(text);
		} else {

			int bestWrap = -1;

			if (" ".equals(text.substring(lineLength, lineLength + 1))) {
				bestWrap = lineLength;
			} else {
				bestWrap = text.substring(0, lineLength).lastIndexOf(" ");
			}

			if (bestWrap == -1) {
				bestWrap = text.indexOf(" ");
			}

			if (bestWrap > 0) {
				output.append(text.substring(0, bestWrap)).append("\n");
				for (int i = 0; i < nextLinePrefix; ++i) {
					output.append(" ");
				}
				wrap(output, text.substring(bestWrap), nextLinePrefix, lineLength);
			} else {
				output.append(text);
			}

		}
	}
}
