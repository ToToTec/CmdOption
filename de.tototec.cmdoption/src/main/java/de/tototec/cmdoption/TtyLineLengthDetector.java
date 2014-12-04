package de.tototec.cmdoption;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tototec.cmdoption.internal.Logger;
import de.tototec.cmdoption.internal.LoggerFactory;

public class TtyLineLengthDetector implements LineLengthDetector {

	private final Logger log = LoggerFactory.getLogger(TtyLineLengthDetector.class);

	public Integer detectOrNull() {

		final File tty = new File("/dev/tty");
		if (!tty.exists()) {
			log.debug("/dev/tty does not exist");
			return null;
		}

		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final ProcessBuilder pb = new ProcessBuilder("stty", "-a", "-F", tty.getPath());
			final Process process = pb.start();
			final InputStream pOut = process.getInputStream();
			copy(pOut, out);
			process.waitFor();
			pOut.close();

			final String stty = out.toString();
			log.trace("Output of stty: {}", stty);

			// this pattern doesn'tr work for me, but google says it works for
			// others
			final Pattern pattern1 = Pattern.compile("columns\\s+=\\s+([^;]*)[;\\n\\r]");
			final Matcher matcher1 = pattern1.matcher(stty);
			if (matcher1.find()) {
				return Integer.parseInt(matcher1.group(1));
			} else {
				final Pattern pattern2 = Pattern.compile("columns\\s+([^;]*)[;\\n\\r]");
				final Matcher matcher2 = pattern2.matcher(stty);
				if (matcher2.find()) {
					return Integer.parseInt(matcher2.group(1));
				} else {
					return null;
				}
			}

		} catch (final Exception e) {
			log.debug("Could not eval columns with tty", e);
			return null;
		}

	}

	private void copy(final InputStream in, final OutputStream out) throws IOException {
		final byte[] buf = new byte[1024];
		int len = 0;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
	}

}
