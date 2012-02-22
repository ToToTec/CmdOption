package de.tototec.cmdoption;

public class CommandHandle {

	private final String[] names;
	private final String description;
	private final CmdlineParser cmdlineParser;
	private final Object object;

	public CommandHandle(String[] names, String description, CmdlineParser cmdlineParser, Object object) {
		this.names = names;
		this.description = description;
		this.cmdlineParser = cmdlineParser;
		this.object = object;
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

	public Object getObject() {
		return object;
	}
}
