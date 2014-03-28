package com.github.aiderpmsi.pimsdriver.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Callable;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.aiderpmsi.pims.utils.Parser;
import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.github.aiderpmsi.pimsdriver.odb.OdbRsfContentHandler;
import com.github.aiderpmsi.pimsdriver.odb.OdbRssContentHandler;
import com.github.aiderpmsi.pimsdriver.odb.PimsODocumentHelper;
import com.github.aiderpmsi.pimsdriver.pmsi.RecorderErrorHandler;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;

public class ProcessImpl implements Callable<Boolean> {
	
	private ODocument odoc;
	
	public ProcessImpl(ODocument odoc) {
		this.odoc = odoc;
	}

	@Override
	public Boolean call() {
		ODatabaseDocumentTx tx = null;
		try {
			tx = DocDbConnectionFactory.getInstance().getConnection();
			tx.begin();
			
			// ORIENTDB HELPER
			PimsODocumentHelper odocHelper = new PimsODocumentHelper(odoc);

			// IF RSF IS DEFINED, GET ITS CONTENT AND PROCESS IT
			InputStream rsf = null;
			String rsfFiness = null;
			if (odoc.field("rsf") != null) {
				rsf = odocHelper.getInputStream("rsf");

				// PROCESS RSF
				ContentHandler ch = new OdbRsfContentHandler(tx, (ORID) odoc.field("RID", ORID.class));
				RecorderErrorHandler reh = new RecorderErrorHandler();
				Parser pars = new Parser();
				pars.setStartState("headerrsf");
				pars.setContentHandler(ch);
				pars.setErrorHandler(reh);
				pars.parse(new InputSource(new InputStreamReader(rsf, "ISO-8859-1")));
			
				// CHECK IF THERE WERE ANY ERRORS IN RSF
				if (reh.getErrors().size() != 0)
					throw new SAXException("RSF : " + reh.getErrors().get(0).getMessage());
				
				// GET THE REAL FINESS FROM RSF
				OCommandSQL rsfFinessCommand =
						new OCommandSQL("select Finess from PmsiElement where parentlink=? AND type='rsfheader'");
				List<ODocument> rsfFinessResults = tx.command(rsfFinessCommand).execute((ORID) odoc.field("RID", ORID.class));
				rsfFiness = rsfFinessResults.get(0).field("Finess");
			}
			
			// IF RSS IS DEFINED, GET ITS CONTENT AND PROCESS IT
			InputStream rss = null;
			String rssFiness = null;
			if (odoc.field("rss") != null) {
				rss = odocHelper.getInputStream("rss");

				// PROCESS RSS
				ContentHandler ch = new OdbRssContentHandler(tx, (ORID) odoc.field("RID", ORID.class));
				RecorderErrorHandler reh = new RecorderErrorHandler();
				Parser pars = new Parser();
				pars.setStartState("headerrss");
				pars.setContentHandler(ch);
				pars.setErrorHandler(reh);
				pars.parse(new InputSource(new InputStreamReader(rss, "ISO-8859-1")));
			
				// CHECK IF THERE WERE ANY ERRORS IN RSS
				if (reh.getErrors().size() != 0)
					throw new SAXException("RSS : " + reh.getErrors().get(0).getMessage());
				
				// GET THE REAL FINESS FROM RSS
				OCommandSQL rsfFinessCommand =
						new OCommandSQL("select Finess from PmsiElement where parentlink=? AND type='rssheader'");
				List<ODocument> rsfFinessResults = tx.command(rsfFinessCommand).execute((ORID) odoc.field("RID", ORID.class));
				rssFiness = rsfFinessResults.get(0).field("Finess");
			}
			
			// VERIFY THAT RSF AND RSS FINESS MATCH
			if (rsfFiness != null && rssFiness != null && rsfFiness != rssFiness)
				throw new IOException("Finess dans RSF et RSS ne correspondent pas");
						
			// UPDATE STATUS AND REAL FINESS
			OCommandSQL ocommand =
					new OCommandSQL("update PmsiUpload set processed = 'processed', finess = ? WHERE @RID=?");
			tx.command(ocommand).execute(rsfFiness == null ? rssFiness : rsfFiness, (ORID) odoc.field("RID", ORID.class));
			tx.commit();
		} catch (IOException | SAXException e) {
			// IF WE HAVE AN ERROR, ROLLBACK TRANSACTION AND STORE THE REASON FOR THE FAILURE
			tx.rollback();
			tx.begin();
			OCommandSQL ocommand =
					new OCommandSQL("update PmsiUpload set processed = 'failed', errorComment = ? WHERE @RID=?");
			tx.command(ocommand).execute(e.getMessage(), (ORID) odoc.field("RID", ORID.class));
			tx.commit();
		}
		finally {
			if (tx != null) {
				tx.close();
				tx = null;
			}
		}

		return null;
	}

}
