package com.github.aiderpmsi.pimsdriver.db;

import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.text.Segment;

import org.eclipse.persistence.internal.sessions.remote.ReplaceValueHoldersIterator;

/**
 * Process makeing the calls to database while main process reads pmsi file
 * @author jpc
 *
 */
public abstract class DbLink extends Reader implements Callable<Boolean> {

	/** Root Id in DB */
	protected long pmel_root;

	/** Last inserted id */
	protected long last_id;
	
	/** Prepared statement form connection in constructor */
	private PreparedStatement ps;

	/** Queue serving as an ipc */
	public LinkedBlockingQueue<Entry> queue = new LinkedBlockingQueue<>(1000);

	public DbLink(Connection con, long pmel_root) throws SQLException {
		this.pmel_root = pmel_root;
		// CREATES QUERY
		ps = con.prepareStatement(query);
	}

	@Override
	public Boolean call() throws InterruptedException, SQLException {
	}
	
	@Override
	public int read(char[] cbuf, int off, int len) throws java.io.IOException {
		// CREATES A BUFFER
		for (;;) {
			Entry entry = queue.poll(1, TimeUnit.SECONDS);

			if (entry == null) {
				continue;
			} else if (entry.finished) {
				break;
			} else {
				// SETS THE NEW PARENT FOR THIS ELEMENT
				calculateParent(entry);
				
				// SETS THE VALUES OF QUERY ARGS
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
				ps.setString(5, entry.pmel_content);
				
				// STORE ROW AND GETS INSERTION ID
				ResultSet rs = null;
				try {
					rs = ps.executeQuery();
					rs.next();
					last_id = rs.getLong(1);
				} finally {
					if (rs != null) rs.close();
				}
			}
		}
		return null;
	}

	public void store(Entry entry) {
		queue.offer(entry);
	}
	
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

	public void setQueue(LinkedBlockingQueue<Entry> queue) {
		this.queue = queue;
	}

	protected abstract void calculateParent(Entry entry);

	private static final String query = "INSERT INTO pmel_temp (pmel_root, pmel_parent, pmel_type, pmel_line, pmel_content) "
			+ "VALUES(?, ?, ?, ?::BIGINT, ?) RETURNING pmel_id";

	private static final char[] escapeEscape = {'\\', '\\'};

	private static final char[] escapeDelim = {'\\', '|'};

}
