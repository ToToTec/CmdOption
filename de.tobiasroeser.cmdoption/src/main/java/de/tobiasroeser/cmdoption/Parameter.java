package de.tobiasroeser.cmdoption;

import java.lang.reflect.AccessibleObject;

public class Parameter {

	private int minCount;
	private int maxCount;
	private AccessibleObject element;
	private Class<? extends CmdOptionHandler> cmdOptionHandler;
	private final String description;

	public Parameter(AccessibleObject element, String description,
			int minCount, int maxCount,
			Class<? extends CmdOptionHandler> cmdOptionHandler) {
		super();
		this.element = element;
		this.description = description;
		this.minCount = minCount;
		this.maxCount = maxCount;
		this.cmdOptionHandler = cmdOptionHandler != null ? cmdOptionHandler
				: CmdOptionHandler.class;
	}

	public String getDescription() {
		return description;
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
