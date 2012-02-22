package de.tototec.cmdoption;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.tototec.cmdoption.handler.CmdOptionHandler;

/**
 * An Command line option which optionally supports parameters.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Documented
public @interface CmdOption {

	/**
	 * The names of this option.
	 */
	String[] names() default {};

	/**
	 * The description of the option. You can use variable substitutes (e.g.
	 * ${verbose}) to reference other options (e.g. --verbose).}.
	 */
	String description() default "";

	/**
	 * The arguments (their names) supported by this option.
	 */
	String[] args() default {};

	/**
	 * An {@link CmdOptionHandler} to apply the parsed option to the annotated
	 * field or method. If this is not given, all handler registered for
	 * auto-detect will by tried in order.
	 */
	Class<? extends CmdOptionHandler> handler() default CmdOptionHandler.class;

	/**
	 * The minimal needed count this option is required.
	 */
	int minCount() default 0;

	/**
	 * The maximal allowed count this option can be specified. Use -1 to specify
	 * infinity.
	 */
	int maxCount() default 1;

	boolean isHelp() default false;

	/**
	 * If <code>true</code>, do not show this option in the usage.
	 */
	boolean hidden() default false;

}
