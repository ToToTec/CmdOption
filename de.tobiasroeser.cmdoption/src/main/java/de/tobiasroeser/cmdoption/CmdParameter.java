package de.tobiasroeser.cmdoption;

public @interface CmdParameter {

	/**
	 * The description of the parameter.
	 */
	String description() default "";

	/**
	 * An {@link CmdOptionHandler} to apply the parsed parameter to the
	 * annotated field or method. If this is not given, all handler registered
	 * for auto-detect will by tried in order.
	 */
	Class<? extends CmdOptionHandler> handler() default CmdOptionHandler.class;

	/**
	 * The minimal needed count this parameter is required.
	 */
	int minCount() default 1;

	/**
	 * The maximal allowed count this parameter can be specified. Use -1 to specify
	 * infinity.
	 */
	int maxCount() default 1;

	// /**
	// * The position of this parameter relative to potential other parameters.
	// If
	// * more than one parameter have the same orderWeight, the order of their
	// * occurence in the class will determine the ordering.
	// */
	// int orderWeight() default 1;
}
