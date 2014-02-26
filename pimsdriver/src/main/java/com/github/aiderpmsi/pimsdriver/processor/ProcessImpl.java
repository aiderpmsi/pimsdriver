package com.github.aiderpmsi.pimsdriver.processor;

import java.io.InputStream;
import java.util.concurrent.Callable;

import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.github.aiderpmsi.pimsdriver.odb.PimsODocumentHelper;
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
	public Boolean call() throws Exception {
		ODatabaseDocumentTx tx = null;
		try {
			tx = DocDbConnectionFactory.getInstance().getConnection();
			tx.begin();
			// IF RSF IS DEFINED, GET ITS CONTENT
			PimsODocumentHelper odocHelper = new PimsODocumentHelper(odoc);
			InputStream rsf = null;
			if (odoc.field("rsf") != null)
				rsf = odocHelper.getInputStream("rsf");
			int readed;
			while((readed = rsf.read()) != -1) {
				System.out.print((char) readed);
			}
			// IF RSS IS DEFINED, GET ITS CONTENT
			@SuppressWarnings("unused")
			InputStream rss = null;
			if (odoc.field("rss") != null)
				rss = odocHelper.getInputStream("rss");
			OCommandSQL ocommand =
					new OCommandSQL("update PmsiUpload set processed = 'processed' WHERE @RID=?");
			tx.command(ocommand).execute((ORID)odoc.field("RID"));
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
