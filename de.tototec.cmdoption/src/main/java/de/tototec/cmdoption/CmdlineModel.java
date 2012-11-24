package de.tototec.cmdoption;

import java.util.ArrayList;
import java.util.List;

public class CmdlineModel {

	private final String programName;
	private final List<OptionHandle> options;
	private final List<CommandHandle> commands;
	private final OptionHandle parameter;
	private String aboutLine;

	public CmdlineModel(final String programName, final List<OptionHandle> options, final List<CommandHandle> commands,
			final OptionHandle parameter, final String aboutLine) {
		this.programName = programName;
		this.aboutLine = aboutLine;
		this.options = new ArrayList<OptionHandle>(options);
		this.commands = new ArrayList<CommandHandle>(commands);
		this.parameter = parameter;
	}

	public String getProgramName() {
		return programName;
	}

	public List<OptionHandle> getOptions() {
		return options;
	}

	public List<CommandHandle> getCommands() {
		return commands;
	}

	public OptionHandle getParameter() {
		return parameter;
	}

	public String getAboutLine() {
		return aboutLine;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + //
				"(programName=" + getProgramName() + //
				",options=" + getOptions() + //
				",commands=" + getCommands() + //
				",parameter=" + getParameter() + //
				",aboutLine=" + getAboutLine() + //
				")";

	}

}
