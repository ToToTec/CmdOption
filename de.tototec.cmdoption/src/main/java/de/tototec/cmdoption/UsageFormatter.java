package de.tototec.cmdoption;

import java.util.List;

public interface UsageFormatter {

	public void format(StringBuilder output, String programName, List<OptionHandle> options,
			List<CommandHandle> commands, OptionHandle parameter);

}
