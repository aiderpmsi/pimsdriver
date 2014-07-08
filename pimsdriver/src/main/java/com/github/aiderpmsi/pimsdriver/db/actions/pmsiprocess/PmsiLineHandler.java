package com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;

import com.github.aiderpmsi.pims.parser.linestypes.ConfiguredPmsiLine;
import com.github.aiderpmsi.pims.parser.linestypes.EndOfFilePmsiLine;
import com.github.aiderpmsi.pims.parser.linestypes.IPmsiLine;
import com.github.aiderpmsi.pims.parser.linestypes.LineNumberPmsiLine;
import com.github.aiderpmsi.pims.parser.utils.Utils.LineHandler;

/**
 * Process makeing the calls to database while main process reads pmsi file
 * @author jpc
 *
 */
public abstract class PmsiLineHandler implements LineHandler, AutoCloseable {

	/** Root Id in DB */
	protected final long pmel_root;

	/** Line number */
	protected String lineNumber = null;
	
	/** Position in the pmsi */
	protected Long pmsiPosition = null;
	
	/** Temp file where we will store the results of the handling */
	private final Path tmpFile;
	
	/** Writer associated with this file */
	private final Writer writer;

	/** Indicates if the writer is closed */
	private boolean writerClosed;
	
	public PmsiLineHandler(final long pmel_root, final long pmsiPosition) throws IOException {
		this.pmel_root = pmel_root;
		this.pmsiPosition = pmsiPosition;
		this.tmpFile = Files.createTempFile("", "");
		writer = Files.newBufferedWriter(tmpFile, Charset.forName("UTF-8"),
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		writerClosed = false;
	}

	@Override
	public void handle(final IPmsiLine line) throws IOException {
		// IF WE ARE IN LINENUMBER, STORE THIS LINENUMBER
		if (line instanceof LineNumberPmsiLine) {
			final LineNumberPmsiLine pmsiLine = (LineNumberPmsiLine) line;
			lineNumber = pmsiLine.getLine();
		}
		// IF WE ARE IN A CLASSIC LINE, WRITES THE CONTENT
		else if (line instanceof ConfiguredPmsiLine) {
			// SETS THE NEW PARENT FOR THIS LINE
			calculateParent(line);
			
			// WRITES THE CONTENT
			// 1 - ROOT
			writer.append(Long.toString(pmel_root));
			writer.append('|');

			// 2 - PMSI POSITION (UNIQUE IN EACH ROOT)
			writer.append(Long.toString(pmsiPosition));
			writer.append('|');
		
			// 3 - PARENT (NULL FOR HEADER)
			final Long pmel_parent = getParent();
			writer.append(pmel_parent == null ? "\\N" : Long.toString(pmel_parent));
			writer.append('|');
		
			// 4 - KIND OF CONTENT
			escape(line.getName(), writer);
			writer.append('|');

			// 5 -WRITES LINE NUMBER
			writer.append(lineNumber);
			writer.append('|');
		
			// 6 - WRITES LINE
			escape(line.getMatchedLine(), writer);

			// 7 - END LINE
			writer.append("\r\n");

			pmsiPosition++;
		} else if (line instanceof EndOfFilePmsiLine) {
			// EOF, CLOSE WRITER
			writer.close();
			writerClosed = true;
		}
	}
	
	public <T> T applyOnFile(final Function<Reader, T> function) throws IOException, SQLException {
		if (!writerClosed)
			throw new IllegalArgumentException("Writer is open");
		try (final Reader reader = Files.newBufferedReader(tmpFile, Charset.forName("UTF-8"))) {
			return function.apply(reader);
		}
	}
	
	@Override
	public void close() throws IOException {
		try {
			if (!writerClosed)
				writer.close();
		} finally {
			Files.delete(tmpFile);
		}
	}

	private void escape(final CharSequence sgt, final Writer writer) throws IOException {		
		for (int i = 0 ; i < sgt.length() ; i++) {
			final char character = sgt.charAt(i); 
			if (character == '\\')
				for (final char escapeChar : escapeEscape)
					writer.append(escapeChar);
			else if (character == '|')
				for (final char escapeChar : escapeDelim)
					writer.append(escapeChar);
			else
				writer.append(character);
		}
	}
	
	public Long getPmsiPosition() {
		return pmsiPosition;
	}

	protected abstract Long getParent();

	protected abstract void calculateParent(final IPmsiLine line);
	
	public abstract String getFiness();

	public abstract String getVersion();
	
	private static final char[] escapeEscape = {'\\', '\\'};

	private static final char[] escapeDelim = {'\\', '|'};

	@FunctionalInterface
	public interface Function<T, R> {
		public R apply(T t) throws IOException, SQLException;
	}
}
