package com.github.aiderpmsi.pimsdriver.dao;

import java.util.ArrayList;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class NavigationDAO {

	public List<String> getFiness(String status) {
		OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>("SELECT DISTINCT(finess) AS d_finess FROM PmsiUpload WHERE processed = ?");
		ODatabaseDocumentTx tx = null;
		List<ODocument> results = null;
		try {
			tx = DocDbConnectionFactory.getInstance().getConnection();
			tx.begin();
			results = tx.command(oquery).execute(status);
			tx.commit();
		} finally {
			if (tx != null)
				tx.close();
		}

		List<String> finesses = new ArrayList<>(results.size());
		for (ODocument result : results) {
			finesses.add((String) result.field("d_finess"));
		}

		return finesses;
	}

}
