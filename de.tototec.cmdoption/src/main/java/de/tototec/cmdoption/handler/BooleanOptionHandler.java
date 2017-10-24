package de.tototec.cmdoption.handler;

/**
 * Apply an zero-arg option to an {@link Boolean} (or <code>boolean</code>)
 * field. If the option is present, the field will be evaluated to
 * <code>true</code>.
 *
 */
public class BooleanOptionHandler extends BaseBooleanOptionHandler {

	public BooleanOptionHandler() {
		super(true);
	}

}
