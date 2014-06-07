package com.github.aiderpmsi.pimsdriver.processor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.postgresql.largeobject.LargeObjectManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.aiderpmsi.pims.grouper.model.RssActe;
import com.github.aiderpmsi.pims.grouper.model.RssContent;
import com.github.aiderpmsi.pims.grouper.model.RssDa;
import com.github.aiderpmsi.pims.grouper.model.RssMain;
import com.github.aiderpmsi.pims.grouper.tags.Group;
import com.github.aiderpmsi.pims.grouper.utils.Grouper;
import com.github.aiderpmsi.pims.parser.utils.Parser2;
import com.github.aiderpmsi.pims.treebrowser.TreeBrowserException;
import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.db.PmsiContentHandlerHelper;
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
			Connection innerConn = ((DelegatingConnection<Connection>) con).getInnermostDelegateInternal();
			LargeObjectManager lom = ((org.postgresql.PGConnection)innerConn).getLargeObjectAPI();
			
			// CREATE THE TEMP TABLE TO INSERT THE DATAS
			createTempTablePs.execute();
			
			// PROCESS RSF
			PmsiContentHandlerHelper fch = null;
			long pmsiPosition = 0;
			try {
				fch = new RsfContentHandler(con, element.getRecordid(), pmsiPosition);
				processPmsi(fch, "rsfheader", element.getRsfoid(), lom);
			} finally {
				if (fch != null) {
					fch.close();
					pmsiPosition = fch.getPmsiPosition();
				}
			}

			PmsiContentHandlerHelper rch = null;
			// IF RSS IS DEFINED, GET ITS CONTENT AND PROCESS IT
			if (element.getRssoid() != null) {
				try {
					rch = new RssContentHandler(con, element.getRecordid(), pmsiPosition);
					processPmsi(rch, "rssheader", element.getRssoid(), lom);
				} finally {
					if (rch != null) {
						rch.close();
						pmsiPosition = rch.getPmsiPosition();
					}
				}

				// VERIFY THAT RSF AND RSS FINESS MATCH
				if (!fch.getFiness().equals(rch.getFiness()))
					throw new IOException("Finess dans RSF et RSS ne correspondent pas");
			}

			// CREATE THE PARTITION TO INSERT THE DATAS
			String createPartitionQuery = 
					"CREATE TABLE pmel.pmel_" + element.getRecordid() + " () INHERITS (public.pmel_pmsielement)";
			PreparedStatement createPartitionPs = con.prepareStatement(createPartitionQuery);
			createPartitionPs.execute();

			// COPY TEMP TABLE INTO PMEL TABLE
			String copyQuery = 
					"INSERT INTO pmel.pmel_" + element.getRecordid() + " (pmel_root, pmel_position, pmel_parent, pmel_type, pmel_line, pmel_content, pmel_arguments) \n"
					+ "SELECT pmel_root, pmel_position, pmel_parent, pmel_type, pmel_line, pmel_content, pmel_arguments FROM pmel_temp";
			PreparedStatement copyPs = con.prepareStatement(copyQuery);
			copyPs.execute();
			
			// UPDATE STATUS AND REAL FINESS
			StringBuilder updatequery = new StringBuilder("UPDATE plud_pmsiupload SET plud_processed = 'successed'::plud_status, plud_finess = ?, ");
			updatequery.append("plud_arguments = plud_arguments || hstore(?, ?)");
			if (rch != null)
				updatequery.append(" || hstore(?, ?)");
			updatequery.append(" WHERE plud_id = ?");
			PreparedStatement updateps = con.prepareStatement(updatequery.toString());
			int updateindex = 1;
			updateps.setString(updateindex++, fch.getFiness());
			updateps.setString(updateindex++, "rsfversion");
			updateps.setString(updateindex++, fch.getVersion());
			if (rch != null) {
				updateps.setString(updateindex++, "rssversion");
				updateps.setString(updateindex++, rch.getVersion());
			}
			updateps.setLong(updateindex++, element.getRecordid());
			updateps.execute();

			// CREATE CONSTRAINTS ON PMEL RSF TABLE
			createConstraints(element.getRecordid(), "", con);
			
			// CALCULATE THE GROUP FOR EACH RSS
			String groupQuery = "SELECT smva.numrss, smva.ddn, smva.sexe, smva.dateentree, smva.modeentree, smva.datesortie, \n"
					+ "       smva.modesortie, smva.poidsnouveaune, smva.agegestationnel, \n"
					+ "       smva.nbseances, smva.dp, smva.dr, \n"
					+ "       ARRAY_AGG(CodeCCAM) Acte_CodeCCAM, \n"
					+ "       ARRAY_AGG(sava.Phase) Acte_Phase, \n"
					+ "       ARRAY_AGG(sava.Activite) Acte_Activite, \n"
					+ "       ARRAY_AGG(sdva.DA) DA_DA \n"
					+ "  FROM smva_rssmain_116_view smva \n"
					+ "  LEFT JOIN sava_rssacte_116_view sava ON \n"
					+ "       smva.pmel_position = sava.pmel_parent \n"
					+ "   AND smva.pmel_root = sava.pmel_root \n"
					+ "  LEFT JOIN sdva_rssda_116_view sdva ON \n"
					+ "       smva.pmel_position = sdva.pmel_parent \n"
					+ "   AND smva.pmel_root = sdva.pmel_root \n"
					+ "  WHERE smva.pmel_id = ? \n"
					+ "  GROUP BY smva.pmel_id, smva.pmel_position, smva.numrss, smva.ddn, smva.sexe, smva.dateentree, smva.modeentree, smva.datesortie, \n"
					+ "       smva.modesortie, smva.destination, smva.poidsnouveaune, smva.agegestationnel, \n"
					+ "       smva.nbseances, smva.dp, smva.dr \n"
					+ "  ORDER BY smva.pmel_position;";

			PreparedStatement groupPs = null;
			try {
				groupPs = con.prepareStatement(groupQuery);
				groupPs.setLong(1, element.getRecordid());
				
				ResultSet groupRs = null;
				try {
					groupRs = groupPs.executeQuery();
					
					List<RssContent> buffer = new ArrayList<>();
					String rssNumber = null;
					
					Grouper grouper = new Grouper();
					
					for (;;) {
						boolean hasNext = groupRs.next();
						// IF WE CHANGE OF RSS NUMBER, GROUP THE BUFFER (if buffer is not void)
						if (buffer.size() != 0 &&
								(!hasNext || (rssNumber != null && groupRs.getString(1) != rssNumber))) {
							Group gp = grouper.group(buffer);
							if (gp != null) System.out.println("groupe : " + gp.getRacine());
							buffer.clear();
						}

						if (!hasNext) {
							// IF WE HAVE NO REMAINING RECORDING, GO AWAY
							break;
						} else {
							// WE HAVE TO ADD THE RSS CONTENT FO THE BUFFER
							RssContent rsscontent = new RssContent();
							
							// RSS MAIN
							EnumMap<RssMain, String> rssmain = new EnumMap<>(RssMain.class);
							rssmain.put(RssMain.ddn, groupRs.getString(2));
							rssmain.put(RssMain.sexe, groupRs.getString(3));
							rssmain.put(RssMain.dateentree, groupRs.getString(4));
							rssmain.put(RssMain.modeentree, groupRs.getString(5));
							rssmain.put(RssMain.datesortie, groupRs.getString(6));
							rssmain.put(RssMain.modesortie, groupRs.getString(7));
							rssmain.put(RssMain.poidsnouveaune, groupRs.getString(8));
							rssmain.put(RssMain.agegestationnel, groupRs.getString(9));
							rssmain.put(RssMain.nbseances, groupRs.getString(10));
							rssmain.put(RssMain.dp, groupRs.getString(11));
							rssmain.put(RssMain.dr, groupRs.getString(12));
							rsscontent.setRssmain(rssmain);
							
							// GETS THE CONTENT FOR ACTES
							Array ccamsRs = null;
							Array phasesRs = null;
							Array activitesRs = null;
							try {
								ccamsRs = groupRs.getArray(13);
								String[] ccams = (String[]) ccamsRs.getArray();
								phasesRs = groupRs.getArray(14);
								String[] phases = (String[]) phasesRs.getArray();
								activitesRs = groupRs.getArray(15);
								String[] activites = (String[]) activitesRs.getArray();
								List<EnumMap<RssActe, String>> rssactes = new ArrayList<>();
								for (int i = 0 ; i < ccams.length; i++) {
									if (ccams[i] != null) {
										EnumMap<RssActe, String> rssacte = new EnumMap<>(RssActe.class);
										rssacte.put(RssActe.codeccam, ccams[i]);
										rssacte.put(RssActe.phase, phases[i]);
										rssacte.put(RssActe.activite, activites[i]);
										rssactes.add(rssacte);
									}
								}
								rsscontent.setRssacte(rssactes);
							} finally {
								if (ccamsRs != null) ccamsRs.free();
								if (phasesRs != null) phasesRs.free();
								if (activitesRs != null) activitesRs.free();
							}
							
							// GETS THE CONTENT FOR DAS
							Array dasRs = null;
							try {
								dasRs = groupRs.getArray(16);
								String[] das = (String[]) dasRs.getArray();
								List<EnumMap<RssDa, String>> rssdas = new ArrayList<>();
								for (int i = 0 ; i < das.length ; i++) {
									if (das[i] != null) {
										EnumMap<RssDa, String> rssda = new EnumMap<>(RssDa.class);
										rssda.put(RssDa.da, das[i]);
										rssdas.add(rssda);
									}
								}
								rsscontent.setRssda(rssdas);
							} finally {
								if (dasRs != null) dasRs.free();
							}
							
							buffer.add(rsscontent);
						}
					}
					
				} finally {
					if (groupRs != null) groupRs.close();
				}
				
			} finally {
				if (groupPs != null) groupPs.close();
			}
			
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

	private void createConstraints(long id, CharSequence suffix, Connection con) throws SQLException {
		CharSequence idRepresentation = Long.toString(id);
		CharSequence fullSuffix = new StringBuilder(idRepresentation).append(suffix);
		StringBuilder query = new StringBuilder();
		query.append("ALTER TABLE pmel.pmel_").append(fullSuffix).append('\n').
			append("ADD CONSTRAINT pmel_inherited_").append(fullSuffix).append("_pkey PRIMARY KEY (pmel_id);\n");
		query.append("ALTER TABLE pmel.pmel_").append(fullSuffix).append('\n').
			append("ADD CONSTRAINT pmel_inherited_").append(fullSuffix).append("_line_unique UNIQUE (pmel_position);\n");
		query.append("ALTER TABLE pmel.pmel_").append(fullSuffix).append('\n').
			append("ADD CONSTRAINT pmel_inherited_").append(fullSuffix).append("_pmel_parent_fkey FOREIGN KEY (pmel_parent)\n").
			append("REFERENCES pmel.pmel_").append(fullSuffix).append(" (pmel_position) MATCH SIMPLE\n").
			append("ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED;\n");
		query.append("ALTER TABLE pmel.pmel_").append(fullSuffix).append('\n').
			append("ADD CONSTRAINT pmel_inherited_").append(fullSuffix).append("_pmel_root_fkey FOREIGN KEY (pmel_root)\n").
			append("REFERENCES public.plud_pmsiupload (plud_id) MATCH SIMPLE\n").
			append("ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED;\n");
		query.append("CREATE INDEX pmel_inherited_").append(fullSuffix).append("_pmel_position_idx ON pmel.pmel_").append(fullSuffix).append(" USING btree (pmel_position);\n");
		query.append("ALTER TABLE pmel.pmel_").append(fullSuffix).append('\n').
			append("ADD CONSTRAINT pmel_inherited_").append(fullSuffix).append("_root_check CHECK (pmel_root = ").append(idRepresentation).append(") NO INHERIT;");
		query.append("CREATE INDEX pmel_inherited_").append(fullSuffix).append("_pmel_rsfb_numrss_idx ON pmel.pmel_").append(fullSuffix).
			append(" (substring(pmel_content from 11 for 20)) WHERE pmel_type = 'rsfb';");
		query.append("CREATE INDEX pmel_inherited_").append(fullSuffix).append("_pmel_rssmain_numrss_idx ON pmel.pmel_").append(fullSuffix).
			append(" (substring(pmel_content from 28 for 20)) WHERE pmel_type = 'rssmain';");
		con.prepareCall(query.toString()).execute();
	}
	
	private void processPmsi(PmsiContentHandlerHelper ch, String pmsitype, Long id, LargeObjectManager lom) throws SQLException, SAXException, IOException, TreeBrowserException {
		InputStream dbFile = null;
		Reader tmpFile = null;
		Path tmpPath = null;
		try {
			dbFile = new BufferedInputStream(lom.open(id).getInputStream());
			try {
				tmpPath = Files.createTempFile("", "");
				Files.copy(dbFile, tmpPath, StandardCopyOption.REPLACE_EXISTING);
				try {
					tmpFile = Files.newBufferedReader(tmpPath, Charset.forName("ISO-8859-1"));

					RecorderErrorHandler eh = new RecorderErrorHandler();
					Parser2 parser = new Parser2(pmsitype);
					parser.setContentHandler(ch);
					parser.setErrorHandler(eh);
					parser.parse(new InputSource(tmpFile));

					// CHECK IF THERE WERE ANY ERRORS IN PMSI
					if (eh.getErrors().size() != 0)
						throw new SAXException("Processing " + pmsitype + " : error " + eh.getErrors().get(0).getMessage());
					
				} finally {
					if (tmpFile != null) tmpFile.close();
				}
			} finally {
				if (tmpPath != null) Files.delete(tmpPath);
			}
		} finally {
			if (dbFile != null) dbFile.close();
		}

	}
	
	private static final String createTempTableQuery =
			"CREATE TEMPORARY TABLE pmel_temp ( \n"
			+ " pmel_root bigint NOT NULL, \n"
			+ " pmel_position bigint NOT NULL, \n"
			+ " pmel_parent bigint, \n"
			+ " pmel_type character varying NOT NULL, \n"
			+ " pmel_line bigint NOT NULL, \n"
			+ " pmel_content character varying NOT NULL, \n"
			+ " pmel_arguments hstore NOT NULL DEFAULT hstore('')\n"
			+ ") ON COMMIT DROP;";

}
