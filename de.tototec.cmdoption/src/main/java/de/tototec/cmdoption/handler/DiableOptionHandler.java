package de.tototec.cmdoption.handler;

/**
 * Apply an zero-arg option to an {@link Boolean} (or <code>boolean</code>)
 * field. If the option is present, the field will be evaluated to
 * <code>false</code>. This is especially useful, if you want to provide some
 * "--no-option" semantic.git
 *
 */
public class DiableOptionHandler extends BaseBooleanOptionHandler {

	public DiableOptionHandler() {
		super(false);
	}

}
