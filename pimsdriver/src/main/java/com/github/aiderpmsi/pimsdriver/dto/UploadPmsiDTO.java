package com.github.aiderpmsi.pimsdriver.dto;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.servlet.ServletContext;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadPmsi;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;

public class UploadPmsiDTO extends AutoCloseableDto<UploadPmsiDTO.Query> {

	public enum Query implements StatementProvider {
		INSERT_PLUD;

		@Override
		public String getStatement(Entry<?>... entries) throws SQLException {
			switch (this) {
			case INSERT_PLUD:
				return "INSERT INTO plud_pmsiupload("
				+ "plud_processed, plud_finess, plud_year, plud_month, "
				+ "plud_dateenvoi, plud_rsf_oid, plud_rss_oid) "
				+ "VALUES (?::public.plud_status, ?, ?, ?, "
				+ "transaction_timestamp(), ?, ?) "
				+ "RETURNING plud_id;";
			default: //SHOULD NEVER REACH THIS POINT
				throw new RuntimeException("This code should never been reached");
			}
		}
	};
	
	public UploadPmsiDTO(final Connection con, final ServletContext context) {
		super(con, UploadPmsiDTO.Query.class, context);
	}

	public Long create(UploadPmsi model, InputStream rsf, InputStream rss) throws SQLException {
		
		// USE THE LARGE OBJECT INTERFACE FOR FILES
		@SuppressWarnings("unchecked")
		final Connection conn = ((DelegatingConnection<Connection>) getConnection()).getInnermostDelegateInternal();
		final LargeObjectManager lom = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();

		try {
			// CREATES AND FILLS THE RSF LARGE OBJECT (IF IT EXISTS)
			final Long rsfoid = store(lom, rsf), rssoid = store(lom, rss); 
	
			// CREATES THE PREPARED STATEMENT
			final PreparedStatement ps = getPs(Query.INSERT_PLUD);
	
			ps.setString(1, UploadedPmsi.Status.pending.toString());
			ps.setString(2, model.finess);
			ps.setInt(3, model.year);
			ps.setInt(4, model.month);
			ps.setObject(5, rsfoid, Types.BIGINT);
			ps.setObject(6, rssoid, Types.BIGINT);
				
			// EXECUTE QUERY
			try (final ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getLong(1);
			}
		} catch (IOException e) {
			throw new SQLException(e);
		}
	}

	/**
	 * Stores the inputstream in objectmanager if inputstream is not null
	 * @param lom
	 * @param is
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	private Long store(LargeObjectManager lom, InputStream is) throws IOException, SQLException {
		Long oid = null;
	
		if (is != null) {
			// CREATE THE LARGE OBJECT
			oid = lom.createLO();
			LargeObject lo = lom.open(oid, LargeObjectManager.WRITE);

			// COPY FROM INPUTSTREAM TO LARGEOBJECT
			byte buf[] = new byte[2048];
			int s = 0;
			while ((s = is.read(buf, 0, 2048)) > 0) {
				lo.write(buf, 0, s);
			}
		}
			
		return oid;
	}

}
