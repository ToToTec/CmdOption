package de.tototec.cmdoption;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * @deprecated This class helps to maintain an deprecated API contract and will
 *             be removed together with the deprecated class
 *             {@link UsageFormatter} as soon as possible.
 */
@Deprecated
public class StringBuilderUsageFormatter implements UsageFormatter, UsageFormatter2 {

	private final UsageFormatter2 underlying;

	public StringBuilderUsageFormatter(final UsageFormatter2 underlying) {
		this.underlying = underlying;
	}

	public void format(final PrintStream output, final CmdlineModel cmdlineModel) {
		underlying.format(output, cmdlineModel);
	}

	public void format(final StringBuilder output, final CmdlineModel cmdlineModel) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos);
		underlying.format(ps, cmdlineModel);
		ps.flush();
		output.append(new String(baos.toByteArray(), Charset.forName("UTF-8")));
	}

}
