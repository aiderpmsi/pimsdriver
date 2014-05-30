package com.github.aiderpmsi.pimsdriver.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Process makeing the calls to database while main process reads pmsi file
 * @author jpc
 *
 */
public abstract class DbLink implements Callable<Boolean> {

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
				ps.setLong(1, pmel_root);
				Long pmel_parent;
				if ((pmel_parent = getParent()) == null)
					ps.setNull(2, Types.BIGINT);
				else
					ps.setLong(2, pmel_parent);
				ps.setString(3, entry.pmel_type);
				ps.setString(4, entry.pmel_line);
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
	
	protected abstract Long getParent();

	public void setQueue(LinkedBlockingQueue<Entry> queue) {
		this.queue = queue;
	}

	protected abstract void calculateParent(Entry entry);

	private static final String query = "INSERT INTO pmel_temp (pmel_root, pmel_parent, pmel_type, pmel_line, pmel_content) "
			+ "VALUES(?, ?, ?, ?::BIGINT, ?) RETURNING pmel_id";

}
