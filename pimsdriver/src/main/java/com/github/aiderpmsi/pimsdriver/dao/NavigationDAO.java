package com.github.aiderpmsi.pimsdriver.dao;

import java.util.ArrayList;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.odb.DataSourceSingleton;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class NavigationDAO {

	public class YM {
		public Integer year;
		public Integer month;
	}
	
	public List<String> getFiness(String status) {
		OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>("SELECT DISTINCT(finess) AS d_finess FROM PmsiUpload WHERE processed = ?");
		ODatabaseDocumentTx tx = null;
		List<ODocument> results = null;
		try {
			tx = DataSourceSingleton.getInstance().getConnection();
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

	public List<YM> getYM(String status, String finess) {
		OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>(
				"select distinct($d) AS ym,year, month from PmsiUpload let $d = year.append(' ').append(month)"
				+ " WHERE processed = ? AND finess = ?"
				+ " ORDER BY year DESC, month DESC");
		ODatabaseDocumentTx tx = null;
		List<ODocument> results = null;
		try {
			tx = DataSourceSingleton.getInstance().getConnection();
			tx.begin();
			results = tx.command(oquery).execute(status, finess);
			tx.commit();
		} finally {
			if (tx != null)
				tx.close();
		}

		// IF NO RESULT, THIS FINESS DOESN'T EXIST ANYMORE, RETURN NULL VALUE
		if (results.size() == 0) {
			return null;
		} else {
			List<YM> yms = new ArrayList<>(results.size());
			for (ODocument result : results) {
				YM ym = new YM();
				ym.year = (Integer) result.field("year");
				ym.month = (Integer) result.field("month");
				yms.add(ym);
			}
			return yms;
		}
	}
	
	
}
