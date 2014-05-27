package com.github.aiderpmsi.pimsdriver.processor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.postgresql.largeobject.LargeObjectManager;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.aiderpmsi.pims.grouper.model.RssContent;
import com.github.aiderpmsi.pims.grouper.tags.Group;
import com.github.aiderpmsi.pims.grouper.utils.Grouper;
import com.github.aiderpmsi.pims.parser.utils.Parser;
import com.github.aiderpmsi.pims.treebrowser.TreeBrowserException;
import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.db.RsfContentHandler;
import com.github.aiderpmsi.pimsdriver.db.RssContentHandler;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.pmsi.RecorderErrorHandler;

public class ProcessImpl implements Callable<Boolean> {
	
	private UploadedPmsi element;
	
	public ProcessImpl(UploadedPmsi element) {
		this.element = element;
	}

	@Override
	public Boolean call() throws Exception {
		Connection con = null;
		
		try {
			// GETS THE DB CONNECTION
			con = DataSourceSingleton.getInstance().getConnection();

			// CREATES THE REUSABLE QUERIES
			PreparedStatement createTempTablePs = con.prepareStatement(createTempTableQuery); // CREATE THE TEMP TABLE TO INSERT THE DATAS
			
			// GETS THE LARGE OBJECT INTERFACE FOR FILES
			@SuppressWarnings("unchecked")
			Connection conn = ((DelegatingConnection<Connection>) con).getInnermostDelegateInternal();
			LargeObjectManager lom = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
			
			// CREATE THE TEMP TABLE TO INSERT THE DATAS
			createTempTablePs.execute();
			
			// RSF AND RSS FINESSES RETRIEVED FROM RSS AND RSF FILES
			String rsfFiness = null, rssFiness = null;
			
			// PROCESS RSF
			InputStream rsfis = lom.open(element.getRsfoid()).getInputStream();
			RsfContentHandler fch = null;
			try {
				fch = new RsfContentHandler(con, element.getRecordid());
				rsfFiness = processPmsi(fch, "rsfheader", new BufferedInputStream(rsfis), "rsfheader", con);
			} finally {
				if (fch != null) fch.close();
			}

			// IF RSS IS DEFINED, GET ITS CONTENT AND PROCESS IT
			if (element.getRssoid() != null) {
				InputStream rssis = lom.open(element.getRssoid(), LargeObjectManager.READ).getInputStream();
				RssContentHandler rch = null;
				try {
					rch = new RssContentHandler(con, element.getRecordid());
					rssFiness = processPmsi(rch, "rssheader", new BufferedInputStream(rssis), "rssheader", con);
				} finally {
					if (rch != null) rch.close();
				}
				
				// VERIFY THAT RSF AND RSS FINESS MATCH
				if (!rsfFiness.equals(rssFiness))
					throw new IOException("Finess dans RSF et RSS ne correspondent pas");

				// LISTS EACH PMEL_ID
				PreparedStatement rssMainListPs = con.prepareStatement(rssMainListQuery);
				ResultSet rssMainListResult = rssMainListPs.executeQuery();

				// GROUPER
				Grouper gp = new Grouper();

				// PREPARE THE QUERY TO GET RSS CONTENT
				PreparedStatement rssContentPS = con.prepareStatement(rssContentQuery);

				// PREPARE THE QUERY TO UPDATE DB RECORD AFTER GROUPING
				PreparedStatement updateRssPS = con.prepareStatement(updateRssQuery);
				
				// GROUP EACH MAIN ID
				while (rssMainListResult.next()) {
					// GET THE RSS CONTENT
					RssContent content = readRssContent(rssContentPS, element, rssMainListResult.getLong(1));
					List<RssContent> rums = new ArrayList<>(1);
					rums.add(content);

					// GROUP IT
					Group result;
					try {
						result = gp.group(rums);
					} catch (Exception e) {
						result = new Group();
						result.setErreur(e.getMessage());
					}
					
					// UPDATE GROUP IN DB RECORD
					updateRssPS.setString(1, result.getRacine());
					updateRssPS.setString(2, result.getGravite());
					updateRssPS.setString(3, result.getModalite());
					updateRssPS.setString(4, result.getErreur());
					updateRssPS.setLong(5, rssMainListResult.getLong(1));
					updateRssPS.execute();
				}
				
			}

			// CREATE THE PARTITION TO INSERT THE DATAS
			String createPartitionQuery = 
					"CREATE TABLE pmel.pmel_" + element.getRecordid() + " () INHERITS (public.pmel_pmsielement)";
			PreparedStatement createPartitionPs = con.prepareStatement(createPartitionQuery);
			createPartitionPs.execute();
			
			// COPY TEMP TABLE INTO PMEL TABLE
			String copyQuery = 
					"INSERT INTO pmel.pmel_" + element.getRecordid() + " (pmel_id, pmel_root, pmel_parent, pmel_type, pmel_attributes) \n"
					+ "SELECT pmel_id, pmel_root, pmel_parent, pmel_type, pmel_attributes FROM pmel_temp";
			PreparedStatement copyPs = con.prepareStatement(copyQuery);
			copyPs.execute();
			
			// UPDATE STATUS AND REAL FINESS
			String updatequery = "UPDATE plud_pmsiupload SET plud_processed = 'successed'::plud_status, plud_finess = ? WHERE plud_id = ?";
			PreparedStatement updateps = con.prepareStatement(updatequery);
			updateps.setString(1, rsfFiness);
			updateps.setLong(2, element.getRecordid());
			updateps.execute();

			// CREATE CONSTRAINTS ON PMEL TABLE
			String createPartitionConstraints = 
					"ALTER TABLE pmel.pmel_" + element.getRecordid() + " \n"
					+ "ADD CONSTRAINT pmel_inherited_" + element.getRecordid() + "_pkey PRIMARY KEY (pmel_id); \n"
					+ "ALTER TABLE pmel.pmel_" + element.getRecordid() + " \n"
					+ "ADD CONSTRAINT pmel_inherited_" + element.getRecordid() + "_pmel_parent_fkey FOREIGN KEY (pmel_parent) \n"
					+ "  REFERENCES pmel.pmel_" + element.getRecordid() + " (pmel_id) MATCH SIMPLE \n"
					+ "  ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED; \n"
					+ "ALTER TABLE pmel.pmel_" + element.getRecordid() + " \n"
					+ "ADD CONSTRAINT pmel_inherited_" + element.getRecordid() + "_pmel_root_fkey FOREIGN KEY (pmel_root) \n"
					+ "  REFERENCES public.plud_pmsiupload (plud_id) MATCH SIMPLE \n"
					+ "  ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED; \n"
					+ "CREATE INDEX pmel_inherited_" + element.getRecordid() + "pmel_root_idx ON pmel.pmel_" + element.getRecordid() + " USING btree (pmel_root);";
			PreparedStatement createPartitionConstraintsPs = con.prepareStatement(createPartitionConstraints);
			createPartitionConstraintsPs.execute();
			
			// EVERYTHING WENT FINE, COMMIT
			con.commit();
		} catch (IOException | SQLException | SAXException e) {
			// IF THE EXCEPTION IS DUE TO A SERIALIZATION EXCEPTION, WE HAVE TO RETRY THIS TREATMENT
			if (e instanceof SQLException && ((SQLException) e).getSQLState().equals("40001")) {
				// ROLLBACK, BUT RETRY LATER
				try {con.rollback();} catch (SQLException e2) { throw new RuntimeException(e); }
				return false;
			} else {
				try {
					// IF WE HAVE AN ERROR, ROLLBACK TRANSACTION AND STORE THE REASON FOR THE FAILURE
					con.rollback();
					String updatequery = "UPDATE plud_pmsiupload SET plud_processed = 'failed'::plud_status, plud_arguments = plud_arguments || hstore(?, ?) WHERE plud_id = ?";
					PreparedStatement updateps = con.prepareStatement(updatequery);
					updateps.setString(1, "error");
					updateps.setString(2, e.getMessage() == null ? e.getClass().toString() : e.getMessage());
					updateps.setLong(3, element.getRecordid());
					updateps.execute();
					con.commit();
				} catch (SQLException e2) { e2.addSuppressed(e); throw new RuntimeException(e2); }
					throw new Exception("Erreur de gestion du fichier pmsi", e);
			}
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}

		return true;
	}

	private String processPmsi(ContentHandler ch, String pmsitype, InputStream is, String pmsiheader, Connection con) throws SQLException, SAXException, IOException, TreeBrowserException {
		RecorderErrorHandler eh = new RecorderErrorHandler();
		Parser parser = new Parser();
		parser.setType(pmsitype);
		parser.setContentHandler(ch);
		parser.setErrorHandler(eh);
		parser.parse(new InputSource(new InputStreamReader(is, "ISO-8859-1")));

		// CHECK IF THERE WERE ANY ERRORS IN PMSI
		if (eh.getErrors().size() != 0)
			throw new SAXException("Processing " + pmsitype + " : error " + eh.getErrors().get(0).getMessage());

		// GET THE REAL FINESS FROM PMSI
		StringBuilder querybuilder = new StringBuilder(realFinessQuery);
		querybuilder.append("'").append(pmsiheader).append("';");

		// PREPARE THE QUERY
		PreparedStatement realFinessPS = con.prepareStatement(querybuilder.toString());
		
		// EXECUTE QUERY
		ResultSet realFinessRS = realFinessPS.executeQuery();
		String finess;
		if (!realFinessRS.next() || (finess = realFinessRS.getString(1)) == null)
			throw new SAXException("Processing " + pmsitype + " : error impossible to find finess in pmsi header");

		return finess;
	}
	
	private RssContent readRssContent(PreparedStatement ps, UploadedPmsi model, Long rssId) throws SQLException {
		ps.setLong(1, rssId);

		// EXECUTE QUERY
		ResultSet rs = null;
		RssContent overview = new RssContent();
		try {
			rs = ps.executeQuery();
			
			while (rs.next()) {
				// CREATE ONE HASHMAP FROM PMEL_ATTRIBUTES
				Array attributes = rs.getArray(5);
				String[] atts = (String[]) attributes.getArray();
				HashMap<String, String> hm = new HashMap<>();
				for (int i = 0 ; i < atts.length ; i = i + 2) {
					hm.put(atts[i], atts[i + 1]);
				}
				String type = rs.getString(4);
				switch(type) {
				case "rssmain" : overview.setRssmain(hm); break;
				case "rssacte" : overview.getRssacte().add(hm); break;
				case "rssda" : overview.getRssda().add(hm); break;
				case "rssdad" : overview.getRssdad().add(hm); break;
				}
			}
		} finally {
			if (rs != null) rs.close();
		}
		
		return overview; 
	}

	private static final String createTempTableQuery =
			"CREATE TEMPORARY TABLE pmel_temp ( \n"
			+ " pmel_id bigint NOT NULL DEFAULT nextval('plud_pmsiupload_plud_id_seq'::regclass), \n"
			+ " pmel_root bigint NOT NULL, \n"
			+ " pmel_parent bigint, \n"
			+ " pmel_type character varying NOT NULL, \n"
			+ " pmel_attributes hstore NOT NULL \n"
			+ ") ON COMMIT DROP;\n"
			+ "CREATE INDEX ON pmel_temp USING btree (pmel_id);";

	private static final String realFinessQuery = "SELECT pmel_attributes -> 'Finess' AS finess FROM pmel_temp WHERE pmel_type = ";

	private static final String rssMainListQuery = "SELECT pmel_id FROM pmel_temp WHERE pmel_type = 'rssmain'";

	private static final String rssContentQuery = 
			"WITH RECURSIVE rss AS ( \n"
			+ "SELECT pmel_id, pmel_root, pmel_parent, pmel_type, pmel_attributes \n"
			+ "  FROM pmel_temp WHERE pmel_id = ? \n"
			+ "UNION \n"
			+ "SELECT pmel.pmel_id, pmel.pmel_root, pmel.pmel_parent, pmel.pmel_type, pmel.pmel_attributes \n"
			+ "FROM pmel_temp pmel \n"
			+ "JOIN rss rss ON (rss.pmel_id = pmel.pmel_parent) \n"
			+ ") \n"
			+ "SELECT pmel_id, pmel_root, pmel_parent, pmel_type, hstore_to_array(pmel_attributes) FROM rss \n";

	private static final String updateRssQuery = 
			"UPDATE pmel_temp SET pmel_attributes = pmel_attributes || hstore('GroupedGHSRacine'::text, ?::text) || "
			+ "hstore('GroupedGHSGravite'::text, ?::text) || hstore('GroupedGHSModalite'::text, ?::text) || "
			+ "hstore('GroupedGHSErreur'::text, ?::text) WHERE pmel_id = ?";

}
