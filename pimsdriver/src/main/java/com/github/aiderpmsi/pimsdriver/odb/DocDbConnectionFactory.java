package com.github.aiderpmsi.pimsdriver.odb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class DocDbConnectionFactory {

	private final static String location = "local:/tmp/db/odb";

	private final static String user = "admin";
	
	private final static String pwd = "admin";
	
	private static DocDbConnectionFactory con = null;
	
	private ODatabaseDocumentPool pool = null;
	
	protected DocDbConnectionFactory() {
		// CREATE DB IF NECESSARY
		ODatabaseDocumentTx db = null;
		try {
			db = new ODatabaseDocumentTx(location);
			if (!db.exists()) {
				// IF IT IS NOT EXISTING, CREATE IT
				db.create();
				// POPULATE THE CLASSES TYPES
				db.getMetadata().getSchema().createClass("PmsiUpload");
				db.getMetadata().getSchema().createClass("PmsiElement");
			}
		} finally {
			if (db != null)
				db.close();
		}
		// CREATE THE CONNEXION POOL
		pool = new ODatabaseDocumentPool(location, user, pwd);
		pool.setup(0, 10);
	}

	public synchronized ODatabaseDocumentTx getConnection() {
		return pool.acquire();
	}
	
	public static synchronized DocDbConnectionFactory getInstance() {
		if (con == null) {
			con = new DocDbConnectionFactory();
		}
		return con;
	}
		
}
