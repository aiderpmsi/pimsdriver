package com.github.aiderpmsi.pimsdriver.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import com.github.aiderpmsi.pimsdriver.model.ImportPmsiModel;
import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.github.aiderpmsi.pimsdriver.odb.PimsODocumentHelper;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class ImportPmsiDAO {

	public void importPmsi(ImportPmsiModel model, InputStream rsf, InputStream rss) throws IOException {
		ODatabaseDocumentTx db = DocDbConnectionFactory.getInstance().getConnection();
		
		try {
			// TX BEGIN
			db.begin();
			Date now = new Date();
			// CREATES THE ENTRY IN THE RIGHT CLASS
			ODocument odoc = db.newInstance("PmsiUpload");
			// HERLPER FOR THIS DOCUMENT (STORE FILE)
			PimsODocumentHelper odocHelper = new PimsODocumentHelper(odoc);
			odocHelper.field("rsf", rsf);
			if (rss != null)
				odocHelper.field("rss", rss);
			odoc.field("month", model.getMonthValue());
			odoc.field("year", model.getYearValue());
			odoc.field("finess", model.getFinessValue());
			odoc.field("processed", "pending");
			odoc.field("dateenvoi", now);
			// SAVE THIS ENTRY
			db.save(odoc);
			// TX END
			db.commit();
		} finally {
			db.close();
		}

	}


}
