package de.tobiasroeser.cmdoption;

import java.lang.reflect.AccessibleObject;

/**
 * An Handler is needed to parse an Commandline option and to apply the parser
 * result to the annotated field or method.
 * 
 */
public interface CmdOptionHandler {

	/**
	 * Return <code>true</code> if the field or method (<code>element</code>)
	 * with the given number of arguments (<code>argCount</code>) can be handled
	 * by this handler.
	 * 
	 * @param element
	 *            The field or method that represents an command line option.
	 * @param argCount
	 *            The number or arguments the command line option supports.
	 * @return <code>true</code> if the option can be parsed and applied by this
	 *         handler.
	 */
	boolean canHandle(AccessibleObject element, int argCount);

	/**
	 * Apply the option and it arguments (if any) to the field or method
	 * representing the option.
	 * 
	 * @param config
	 *            The object containing the field or element to which the parsed
	 *            values should be applied.
	 * @param element
	 *            The element itself (field or method) to apply to.
	 * @param args
	 *            The parsed arguments of the option.
	 */
	void applyParams(Object config, AccessibleObject element, String... args);
}
