package com.github.aiderpmsi.pimsdriver.db;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.text.Segment;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.postgresql.copy.CopyManager;

/**
 * Process makeing the calls to database while main process reads pmsi file
 * @author jpc
 *
 */
public abstract class DbLink extends Reader implements Callable<Boolean> {

	/** Root Id in DB */
	protected long pmel_root;

	/** Last inserted line */
	protected long last_line;
	
	/** Copy Manager */
	private CopyManager cm;

	/** Queue serving as an ipc */
	public LinkedBlockingQueue<Entry> queue = new LinkedBlockingQueue<>(1000);

	/** Buffer for readed elements remaining */
	private CharSequence remaining = new Segment(); 

	/** Indicates if the end element has been reached */
	boolean end = false;
	
	public DbLink(Connection con, long pmel_root) throws SQLException {
		@SuppressWarnings("unchecked")
		Connection conn = ((DelegatingConnection<Connection>) con).getInnermostDelegateInternal();
		cm = new CopyManager((org.postgresql.core.BaseConnection)conn);
		this.pmel_root = pmel_root;
	}

	@Override
	public Boolean call() throws InterruptedException, SQLException, IOException {
		// CREATES QUERY
		cm.copyIn(query, this);

		if (Thread.interrupted())
			throw new InterruptedException();
		
		return null;
	}
	
	@Override
	public int read(char[] cbuf, int off, int len) throws java.io.IOException {
		// WE ARE OUT OF STREAM
		if (end)
			return -1;

		int position = off;
		int last_position = off + len;
		
		while (position < last_position) {
			if (remaining.length() != 0) {
				// FIRST TRY TO ADD REMAINING DATAS
				cbuf[position] = remaining.charAt(0);
				remaining = remaining.subSequence(1, remaining.length());
				position++;
			} else {
				// NOTHING TO ADD, FILL THE BUFFER AGAIN
				Entry entry;
				try {
					entry = queue.poll(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					// WE ARE INTERRUPTED, STOP READING
					end = true;
					break;
				}

				if (entry == null) {
					continue;
				} else if (entry.finished) {
					// WE ARE AT END OF STREAM
					end = true;
					break;
				}

				// SETS THE NEW PARENT FOR THIS ELEMENT
				calculateParent(entry);
				
				// SETS THE VALUES OF ELEMENTS
				StringBuilder content = new StringBuilder();
				content.append(Long.toString(pmel_root));
				content.append('|');
				
				Long pmel_parent;
				if ((pmel_parent = getParent()) == null)
					content.append("\\N");
				else
					content.append(Long.toString(pmel_parent));
				content.append('|');

				escape(entry.pmel_type, content);
				content.append('|');

				content.append(entry.pmel_line);
				content.append('|');
				
				escape(entry.pmel_content, content);
				
				content.append("\r\n");
				
				// STORE ROW AND GETS LINE NUMBER
				remaining = content;
				
				last_line = Long.parseLong(entry.pmel_line);
			}
		}
		return position - off;
	}

	public void store(Entry entry) {
		queue.offer(entry);
	}

	@Override
	public void close() throws IOException {
		// DO NOTHING
	};
	
	private void escape(CharSequence sgt, StringBuilder append) {		
		int size = sgt.length();
		for (int i = 0 ; i < size ; i++) {
			char character = sgt.charAt(i); 
			if (character == '\\')
				append.append(escapeEscape);
			else if (character == '|')
				append.append(escapeDelim);
			else
				append.append(character);
		}
	}
	
	protected abstract Long getParent();

	protected abstract void calculateParent(Entry entry);

	private static final String query = "COPY pmel_temp (pmel_root, pmel_parent, pmel_type, pmel_line, pmel_content) "
			+ "FROM STDIN WITH DELIMITER '|'";

	private static final char[] escapeEscape = {'\\', '\\'};

	private static final char[] escapeDelim = {'\\', '|'};

}
