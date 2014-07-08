package com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess;

import java.io.IOException;
import java.io.Writer;

import com.github.aiderpmsi.pims.parser.linestypes.ConfiguredPmsiLine;
import com.github.aiderpmsi.pims.parser.linestypes.IPmsiLine;
import com.github.aiderpmsi.pims.parser.linestypes.LineNumberPmsiLine;
import com.github.aiderpmsi.pims.parser.utils.Utils.LineHandler;

/**
 * Process makeing the calls to database while main process reads pmsi file
 * @author jpc
 *
 */
public abstract class PmsiLineHandler implements LineHandler {

	/** Root Id in DB */
	protected final long pmel_root;

	/** Line number */
	protected String lineNumber = null;
	
	/** Position in the pmsi */
	protected Long pmsiPosition = null;
	
	/** Writer to write in */
	private Writer writer = null;
	
	public PmsiLineHandler(final long pmel_root, final long pmsiPosition) {
		this.pmel_root = pmel_root;
		this.pmsiPosition = pmsiPosition;
	}

	public void setWriter(final Writer writer) {
		this.writer = writer;
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
		}
	}

	private void escape(CharSequence sgt, Writer writer) throws IOException {		
		int size = sgt.length();
		for (int i = 0 ; i < size ; i++) {
			char character = sgt.charAt(i); 
			if (character == '\\')
				for (char escapeChar : escapeEscape)
					writer.append(escapeChar);
			else if (character == '|')
				for (char escapeChar : escapeDelim)
					writer.append(escapeChar);
			else
				writer.append(character);
		}
	}
	
	public Long getPmsiPosition() {
		return pmsiPosition;
	}

	protected abstract Long getParent();

	protected abstract void calculateParent(IPmsiLine line);
	
	public abstract String getFiness();

	public abstract String getVersion();
	
	private static final char[] escapeEscape = {'\\', '\\'};

	private static final char[] escapeDelim = {'\\', '|'};

}
