package com.github.aiderpmsi.pimsdriver.dto;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import javax.servlet.ServletContext;

import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.postgresql.largeobject.LargeObjectManager;

import com.github.aiderpmsi.pims.parser.utils.SimpleParser;
import com.github.aiderpmsi.pims.parser.utils.SimpleParserFactory;
import com.github.aiderpmsi.pims.parser.utils.Utils.LineHandler;
import com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess.GroupHandler;
import com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess.PmsiLineHandler;
import com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess.SimpleErrorHandler;
import com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess.SimpleErrorHandler.Error;
import com.github.aiderpmsi.pimsdriver.dto.StatementProvider.Entry;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi.Status;

public class ProcessorDTO extends AutoCloseableDto<ProcessorDTO.Query> {

	public enum Query implements StatementProvider {
		CREATE_TEMP_TABLES,
		CREATE_DEFINITIVE_TABLES,
		COPY_TEMP_TABLES,
		CREATE_CONSTRAINTS,
		SET_STATUS;

		@Override
		public String getStatement(Entry<?>... entries) throws SQLException {
			if (this == CREATE_TEMP_TABLES) {
				return "CREATE TEMPORARY TABLE pmel_temp ( \n"
				+ " pmel_root bigint NOT NULL, \n"
				+ " temp_numrss character varying, \n"
				+ " pmel_position bigint NOT NULL, \n"
				+ " pmel_parent bigint, \n"
				+ " pmel_type character varying NOT NULL, \n"
				+ " pmel_line bigint NOT NULL, \n"
				+ " pmel_content character varying NOT NULL, \n"
				+ " pmel_arguments hstore NOT NULL DEFAULT hstore('')\n"
				+ ") ON COMMIT DROP;"
				+ "CREATE TEMPORARY TABLE pmgr_temp ( \n"
				+ " pmel_position bigint NOT NULL, \n"
				+ " pmgr_racine character varying NOT NULL, \n"
				+ " pmgr_modalite character varying NOT NULL, \n"
				+ " pmgr_gravite character varying NOT NULL, \n"
				+ " pmgr_erreur character varying NOT NULL \n"
				+ ") ON COMMIT DROP;";
			} else if (this == SET_STATUS) {
				return "UPDATE plud_pmsiupload SET plud_processed = ?::plud_status, plud_finess = ?, "
						+ "plud_arguments = plud_arguments || hstore(?) WHERE plud_id = ?";
			} else if (entries.length == 1
					&& entries[0] != null && entries[0].object != null && entries[0].object instanceof Long) {
				switch(this) {
				case CREATE_DEFINITIVE_TABLES:
					return "CREATE TABLE pmel.pmel_" + entries[0].object + " () INHERITS (public.pmel_pmsielement);\n"
					+ "CREATE TABLE pmgr.pmgr_" + entries[0].object + " () INHERITS (public.pmgr_pmsigroups);";
				case COPY_TEMP_TABLES:
					return "INSERT INTO pmel.pmel_" + entries[0].object + " (pmel_root, pmel_position, pmel_parent, pmel_type, pmel_line, pmel_content, pmel_arguments) \n"
					+ "SELECT pmel_root, pmel_position, pmel_parent, pmel_type, pmel_line, pmel_content, pmel_arguments FROM pmel_temp;\n"
					+ "INSERT INTO pmgr.pmgr_" + entries[0].object + " (pmel_id, pmel_root, pmgr_racine, pmgr_modalite, pmgr_gravite, pmgr_erreur) \n"
					+ "SELECT pmel.pmel_id, " + entries[0].object + ", pmgr.pmgr_racine, pmgr.pmgr_modalite, pmgr.pmgr_gravite, pmgr.pmgr_erreur \n"
					+ "FROM pmgr_temp pmgr \n"
					+ "JOIN pmel.pmel_" + entries[0].object + " pmel ON \n"
					+ "pmgr.pmel_position = pmel.pmel_position;";
				case CREATE_CONSTRAINTS:
					return "ALTER TABLE pmel.pmel_" + entries[0].object + '\n'
					+ "ADD CONSTRAINT pmel_inherited_" + entries[0].object + "_pkey PRIMARY KEY (pmel_id);\n"
					+ "ALTER TABLE pmel.pmel_" + entries[0].object + '\n'
					+ "ADD CONSTRAINT pmel_inherited_" + entries[0].object + "_line_unique UNIQUE (pmel_position);\n"
					+ "ALTER TABLE pmel.pmel_" + entries[0].object + '\n'
					+ "ADD CONSTRAINT pmel_inherited_" + entries[0].object + "_pmel_parent_fkey FOREIGN KEY (pmel_parent)\n"
					+ "REFERENCES pmel.pmel_" + entries[0].object + " (pmel_position) MATCH SIMPLE\n"
					+ "ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED;\n"
					+ "ALTER TABLE pmel.pmel_" + entries[0].object + '\n'
					+ "ADD CONSTRAINT pmel_inherited_" + entries[0].object + "_pmel_root_fkey FOREIGN KEY (pmel_root)\n"
					+ "REFERENCES public.plud_pmsiupload (plud_id) MATCH SIMPLE\n"
					+ "ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED;\n"
					+ "CREATE INDEX pmel_inherited_" + entries[0].object + "_pmel_position_idx ON pmel.pmel_" + entries[0].object + " USING btree (pmel_position);\n"
					+ "ALTER TABLE pmel.pmel_" + entries[0].object + '\n'
					+ "ADD CONSTRAINT pmel_inherited_" + entries[0].object + "_root_check CHECK (pmel_root = " + entries[0].object + ") NO INHERIT;"
					+ "CREATE INDEX pmel_inherited_" + entries[0].object + "_pmel_rsfb_numrss_idx ON pmel.pmel_" + entries[0].object + " (substring(pmel_content from 11 for 20)) WHERE pmel_type = 'rsfb';\n"
					+ "CREATE INDEX pmel_inherited_" +entries[0].object  + "_pmel_rssmain_numrss_idx ON pmel.pmel_" + entries[0].object + " (substring(pmel_content from 28 for 20)) WHERE pmel_type = 'rssmain';\n"
					+ "ALTER TABLE pmgr.pmgr_" + entries[0].object + '\n'
					+ " ADD CONSTRAINT pmgr_inherited_" + entries[0].object + "_root_check CHECK (pmel_root = " + entries[0].object + ") NO INHERIT;\n"
					+ "ALTER TABLE pmgr.pmgr_" + entries[0].object + '\n'
					+ " ADD CONSTRAINT pmgr_inherited_" + entries[0].object + "_pkey PRIMARY KEY (pmgr_id);\n"
					+ "ALTER TABLE pmgr.pmgr_" + entries[0].object + '\n'
					+ " ADD CONSTRAINT pmgr_inherited_" + entries[0].object + "_pmel_id_unique UNIQUE (pmel_id);\n"
					+ "ALTER TABLE pmgr.pmgr_" + entries[0].object + '\n'
					+ " ADD CONSTRAINT pmgr_inherited_" + entries[0].object + "_pmel_root_fkey FOREIGN KEY (pmel_root)\n"
					+ " REFERENCES public.plud_pmsiupload (plud_id) MATCH SIMPLE\n"
					+ " ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED;\n"
					+ "ALTER TABLE pmgr.pmgr_" + entries[0].object + '\n'
					+ " ADD CONSTRAINT pmgr_inherited_" + entries[0].object + "_pmel_id_fkey FOREIGN KEY (pmel_id)\n"
					+ " REFERENCES pmel.pmel_" + entries[0].object + " (pmel_id) MATCH SIMPLE\n"
					+ " ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED;\n";
				default:
					throw new RuntimeException("This code should never been reached");
				}
			} else {
				throw new SQLException(this.toString() + " needs one long parameter");
			}
		}
		
	}
	
	public ProcessorDTO(final Connection con, final ServletContext context) {
		super(con, ProcessorDTO.Query.class, context);
	}

	public void createTempTables() throws SQLException {
		PreparedStatement ps = getPs(Query.CREATE_TEMP_TABLES);
		ps.execute();
	}
	
	public void createDefinitiveTables(Long id) throws SQLException {
		Entry<Long> idEntry = new Entry<>();
		idEntry.object = id;
		idEntry.clazz = Long.class;
		
		PreparedStatement ps = getPs(Query.CREATE_DEFINITIVE_TABLES, idEntry);
		ps.execute();
	}
	
	public void copyTempTables(Long id) throws SQLException {
		Entry<Long> idEntry = new Entry<>();
		idEntry.object = id;
		idEntry.clazz = Long.class;
		
		PreparedStatement ps = getPs(Query.COPY_TEMP_TABLES, idEntry);
		ps.execute();
	}
	
	public void createContraints(Long id) throws SQLException {
		Entry<Long> idEntry = new Entry<>();
		idEntry.object = id;
		idEntry.clazz = Long.class;
		
		PreparedStatement ps = getPs(Query.CREATE_CONSTRAINTS, idEntry);
		ps.execute();
	}

	public void processPmsi(String type, Collection<LineHandler> lhs, SimpleParserFactory pf, Long oid) throws SQLException {
		
		Connection innerCon;

		if(getConnection() instanceof DelegatingConnection
				&& (innerCon = ((DelegatingConnection<?>) getConnection()).getInnermostDelegateInternal()) instanceof PGConnection) {
			// GETS LARGE OBJECT API IF CONNECTION IS POSTGRESQL
			LargeObjectManager lom = ((org.postgresql.PGConnection)innerCon).getLargeObjectAPI();
			
			long time1 = System.currentTimeMillis();
			
			try {

				// TRANSFORMS PMSI TO A READY TO IMPORT POSTGRESQL FILE
				try (Reader dbFile = new InputStreamReader(lom.open(oid).getInputStream(), Charset.forName("UTF-8"))) {

					// PARSE AND STORE PMSI
					SimpleErrorHandler eh = new SimpleErrorHandler();
					SimpleParser parser = pf.newParser(type, lhs, eh);
					parser.parse(dbFile);

					// THROW ERROR IF AN ERROR HAPPENED
					if (eh.getErrors().size() != 0) {
						IOException e = null;
						int i = 0;
						for (Error ex : eh.getErrors()) {
							if (i++ == 0)
								e = new IOException(ex.msg + " at line " + ex.line);
							else
								e.addSuppressed(new IOException(ex.msg + " at line " + ex.line));
						}
						throw e;
					}
					
				}
			
				// BULK IMPORT TO PGSQL
				String pmsiQuery = "COPY pmel_temp (pmel_root, pmel_position, pmel_parent, pmel_type, pmel_line, pmel_content) "
						+ "FROM STDIN WITH DELIMITER '|'";
				String groupQuery = "COPY pmgr_temp (pmel_position, pmgr_racine, pmgr_modalite, pmgr_gravite, pmgr_erreur) "
						+ "FROM STDIN WITH DELIMITER '|'";

				final Connection conn = ((DelegatingConnection<?>) getConnection()).getInnermostDelegateInternal();
				final CopyManager cm = new CopyManager((org.postgresql.core.BaseConnection)conn);
				
				for (final LineHandler lineHandler : lhs) {
					if (lineHandler instanceof PmsiLineHandler) {
						((PmsiLineHandler) lineHandler).applyOnFile(
								(reader) -> cm.copyIn(pmsiQuery, reader));
					} else if (lineHandler instanceof GroupHandler) {
						((GroupHandler) lineHandler).applyOnFile(
								(reader) -> cm.copyIn(groupQuery, reader));
					}
				}

			} catch (IOException e) {
				throw new SQLException(e);
			}

			final long time2 = System.currentTimeMillis();
			System.out.println("Difference time : " + (time2 - time1));

		} else {
			throw new RuntimeException("This function needs a Delegated PGConnection");
		}
	}

	public void setStatus(Long recordid, Status successed, String finess,
			Object ... parameters) throws SQLException {
		if ((parameters.length & 1) != 0) {
			// ODD NUMBER OF PARAMETERS
			throw new SQLException("odd number of parameters");
		}
		PreparedStatement ps = getPs(Query.SET_STATUS);
		ps.setString(1, successed.toString());
		ps.setString(2, finess);
		Array parametersArray = getConnection().createArrayOf("text", parameters);
		ps.setArray(3, parametersArray);
		ps.setLong(4, recordid);
		ps.execute();
	}
	
}
