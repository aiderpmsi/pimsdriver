package com.github.aiderpmsi.pimsdriver.processor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.github.aiderpmsi.pims.utils.Parser;
import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.github.aiderpmsi.pimsdriver.odb.OdbContentHandler;
import com.github.aiderpmsi.pimsdriver.odb.PimsODocumentHelper;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;

public class ProcessImpl implements Callable<Boolean> {

	private static final String rsfXslPath = "com/github/aiderpmsi/pimsdriver/processor/rsfrewrite.xsl";
	
	private ODocument odoc;
	
	public ProcessImpl(ODocument odoc) {
		this.odoc = odoc;
	}

	@Override
	public Boolean call() throws Exception {
		ODatabaseDocumentTx tx = null;
		try {
			tx = DocDbConnectionFactory.getInstance().getConnection();
			tx.begin();
			
			// ORIENTDB HELPER
			PimsODocumentHelper odocHelper = new PimsODocumentHelper(odoc);

			// IF RSF IS DEFINED, GET ITS CONTENT
			InputStream rsf = null;
			if (odoc.field("rsf") != null)
				rsf = odocHelper.getInputStream("rsf");
			
			// IF RSS IS DEFINED, GET ITS CONTENT
			@SuppressWarnings("unused")
			InputStream rss = null;
			if (odoc.field("rss") != null)
				rss = odocHelper.getInputStream("rss");
			
			// PROCESS RSF
			ContentHandler ch = new OdbContentHandler(tx);
			XMLReader pars = new Parser();
			
			// USE XALAN FOR TRANSFORMER BECAUSE PIMS GENERATES A
			TransformerFactory tfactory = new net.sf.saxon.TransformerFactoryImpl(new Configuration());
			Transformer rsfTransformer = tfactory.newTransformer(
					new StreamSource(ProcessImpl.class.getClassLoader().getResourceAsStream(rsfXslPath)));
			
			try {
				rsfTransformer.transform(
						new SAXSource(pars, new InputSource(new InputStreamReader(rsf, "ISO-8859-1"))),
						new SAXResult(ch));
			} catch (TransformerException e) {
				e.printStackTrace();
			} catch (RuntimeException e) {
				e.printStackTrace();
				throw e;
			}
			
			OCommandSQL ocommand =
					new OCommandSQL("update PmsiUpload set processed = 'processed' WHERE @RID=?");
			tx.command(ocommand).execute(odoc.field("RID"));
			tx.commit();
		} finally {
			if (tx != null) {
				tx.close();
				tx = null;
			}
		}

		return null;
	}

}
