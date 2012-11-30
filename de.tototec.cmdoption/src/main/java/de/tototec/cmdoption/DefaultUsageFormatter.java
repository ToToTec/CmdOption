package de.tototec.cmdoption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class DefaultUsageFormatter implements UsageFormatter {

	private final I18n i18n = I18nFactory.getI18n(DefaultUsageFormatter.class);

	private final boolean withCommandDetails;

	private int lineLength = 80;
	private int colSpace = 2;
	private int col1Prefix = 2;

	public DefaultUsageFormatter(final boolean withCommandDetails) {
		this.withCommandDetails = withCommandDetails;
	}

	protected String translate(final ResourceBundle resourceBundle, final String string) {
		if (resourceBundle != null) {
			try {
				return resourceBundle.getString(string);
			} catch (final MissingResourceException e) {
				// no translation available
			}
		}
		return string;
	}

	protected String[] translate(final ResourceBundle resourceBundle, final String[] strings) {
		final String[] translated = new String[strings.length];
		for (int i = 0; i < strings.length; ++i) {
			translated[i] = translate(resourceBundle, strings[i]);
		}
		return translated;
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
			output.append(translate(cmdlineModel.getResourceBundle(), cmdlineModel.getAboutLine())).append("\n\n");
		}

		// Usage
		output.append(i18n.tr("Usage:")).append(" ");
		output.append(cmdlineModel.getProgramName() == null ? i18n.tr("program") : cmdlineModel.getProgramName());
		if (!sortedOptions.isEmpty()) {
			output.append(" ").append(i18n.tr("[options]"));
		}
		if (cmdlineModel.getParameter() != null) {
			output.append(" ").append(i18n.tr("[parameter]"));
		}
		if (!sortedCommands.isEmpty()) {
			output.append(" ").append(i18n.tr("[command]"));
			boolean cmdsHaveOptions = false;
			boolean cmdsHaveParameter = false;
			for (final CommandHandle cmd : sortedCommands) {
				cmdsHaveOptions |= !cmd.getCmdlineParser().getCmdlineModel().getOptions().isEmpty();
				cmdsHaveParameter |= cmd.getCmdlineParser().getCmdlineModel().getParameter() != null;
			}
			if (cmdsHaveOptions) {
				output.append(" ").append(i18n.tr("[command options]"));
			}
			if (cmdsHaveParameter) {
				output.append(" ").append(i18n.tr("[command parameters]"));
			}
		}
		output.append("\n");

		formatOptions(output, sortedOptions, "\n" + i18n.tr("Options:"), cmdlineModel.getResourceBundle());

		formatCommands(output, sortedCommands, "\n" + i18n.tr("Commands:"), cmdlineModel.getResourceBundle());

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

				formatOptions(
						output,
						commandOptions,
						"\n" + i18n.tr("Options for command:") + " "
								+ Util.mkString(command.getNames(), null, ", ", null), cmdlineModel.getResourceBundle());

				formatParameter(
						output,
						command.getCmdlineParser().getCmdlineModel().getParameter(),
						"\n" + i18n.tr("Parameter for command:") + " "
								+ Util.mkString(command.getNames(), null, ", ", null), cmdlineModel.getResourceBundle());
			}
		}

		// Parameter
		formatParameter(output, cmdlineModel.getParameter(), "\n" + i18n.tr("Parameter:"),
				cmdlineModel.getResourceBundle());
	}

	protected void formatParameter(final StringBuilder output, final OptionHandle parameter, final String title,
			final ResourceBundle resourceBundle) {
		if (parameter == null) {
			return;
		}

		output.append(title).append("\n");
		mkSpace(output, col1Prefix);
		output.append(Util.mkString(translate(resourceBundle, parameter.getArgs()), null, " ", null));
		if (parameter.getDescription() != null) {
			mkSpace(output, colSpace);
			output.append(translate(resourceBundle, parameter.getDescription()));
		}
		output.append("\n");
	}

	protected void formatOptions(final StringBuilder output, final List<OptionHandle> options, final String title,
			final ResourceBundle resourceBundle) {
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
			final String argNames = Util.mkString(translate(resourceBundle, option.getArgs()), null, " ", null);
			optionsToFormat.add(new String[] { optionNames + (argNames.length() == 0 ? "" : (" " + argNames)),
					translate(resourceBundle, option.getDescription()) });
		}

		if (!hasOptions) {
			return;
		}

		if (title != null) {
			output.append(title).append("\n");
		}

		formatTable(output, optionsToFormat, col1Prefix, colSpace, lineLength);
	}

	protected void formatCommands(final StringBuilder output, final List<CommandHandle> commands, final String title,
			final ResourceBundle resourceBundle) {
		if (commands == null || commands.isEmpty()) {
			return;
		}

		if (title != null) {
			output.append(title).append("\n");
		}

		final LinkedList<String[]> commandsToFormat = new LinkedList<String[]>();
		for (final CommandHandle option : commands) {
			final String commandNames = Util.mkString(option.getNames(), null, ",", null);
			commandsToFormat.add(new String[] { commandNames, translate(resourceBundle, option.getDescription()) });
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
