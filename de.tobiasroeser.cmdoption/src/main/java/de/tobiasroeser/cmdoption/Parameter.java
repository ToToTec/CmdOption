package de.tobiasroeser.cmdoption;

import java.lang.reflect.AccessibleObject;

public class Parameter {

	private int minCount;
	private int maxCount;
	private AccessibleObject element;
	private Class<? extends CmdOptionHandler> cmdOptionHandler;
	private final String description;
	private final String[] args;

	public Parameter(AccessibleObject element, String[] args,
			String description, int minCount, int maxCount,
			Class<? extends CmdOptionHandler> cmdOptionHandler) {
		super();
		this.element = element;
		this.args = args;
		this.description = description;
		this.minCount = minCount;
		this.maxCount = maxCount;
		this.cmdOptionHandler = cmdOptionHandler != null ? cmdOptionHandler
				: CmdOptionHandler.class;
	}

	public String getDescription() {
		return description;
	}

	public String[] getArgs() {
		return args;
	}

	public int getMinCount() {
		return minCount;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public AccessibleObject getElement() {
		return element;
	}

	public Class<? extends CmdOptionHandler> getCmdOptionHandler() {
		return cmdOptionHandler;
	}

}
