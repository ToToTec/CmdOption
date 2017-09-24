package de.tototec.cmdoption;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * Wrapper that maps implementations of one of the two interfaces
 * {@link UsageFormatter} and {@link UsageFormatter2} to the other.
 *
 * @deprecated This class helps to maintain an deprecated API contract and will
 *             be removed together with the deprecated class
 *             {@link UsageFormatter} as soon as possible.
 */
@Deprecated
public class WrappingUsageFormatter implements UsageFormatter, UsageFormatter2 {

	private final UsageFormatter stringBuildeUsageFormatter;
	private final UsageFormatter2 printStreamUsageFormatter;

	public WrappingUsageFormatter(final UsageFormatter stringBuilderUsageFormatter) {
		this.stringBuildeUsageFormatter = stringBuilderUsageFormatter;
		this.printStreamUsageFormatter = null;
	}

	public WrappingUsageFormatter(final UsageFormatter2 printStreamUsageFormatter) {
		this.stringBuildeUsageFormatter = null;
		this.printStreamUsageFormatter = printStreamUsageFormatter;
	}

	public void format(final PrintStream output, final CmdlineModel cmdlineModel) {
		if (printStreamUsageFormatter != null) {
			printStreamUsageFormatter.format(output, cmdlineModel);
		} else {
			final StringBuilder sb = new StringBuilder();
			stringBuildeUsageFormatter.format(sb, cmdlineModel);
			output.print(sb.toString());
		}
	}

	public void format(final StringBuilder output, final CmdlineModel cmdlineModel) {
		if (stringBuildeUsageFormatter != null) {
			stringBuildeUsageFormatter.format(output, cmdlineModel);
		} else {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final PrintStream ps = new PrintStream(baos);
			printStreamUsageFormatter.format(ps, cmdlineModel);
			ps.flush();
			output.append(new String(baos.toByteArray(), Charset.forName("UTF-8")));
		}
	}

}
