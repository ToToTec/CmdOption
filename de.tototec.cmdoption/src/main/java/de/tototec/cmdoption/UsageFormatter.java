package de.tototec.cmdoption;

/**
 * @deprecated Use {@link UsageFormatter2} instead.
 *
 * @see CmdlineParser#setUsageFormatter(UsageFormatter2)
 *
 */
@Deprecated
public interface UsageFormatter {

	public void format(final StringBuilder output, final CmdlineModel cmdlineModel);

}
