package com.github.aiderpmsi.pimsdriver.processor;

import java.io.InputStream;
import java.util.concurrent.Callable;

import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.github.aiderpmsi.pimsdriver.odb.PimsODocumentHelper;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class ProcessImpl implements Callable<Boolean> {

	private ODocument odoc;
	
	public ProcessImpl(ODocument odoc) {
		this.odoc = odoc;
	}

	@Override
	public Boolean call() throws Exception {
		ODatabaseDocumentTx tx = null;
		try {
			tx = DocDbConnectionFactory.getConnection();
			tx.begin();
			// IF RSF IS DEFINED, GET ITS CONTENT
			PimsODocumentHelper odocHelper = new PimsODocumentHelper(odoc);
			InputStream rsf = null;
			if (odoc.field("rsf") != null)
				rsf = odocHelper.getInputStream("rsf");
			// IF RSS IS DEFINED, GET ITS CONTENT
			InputStream rss = null;
			if (odoc.field("rss") != null)
				rss = odocHelper.getInputStream("rss");
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
