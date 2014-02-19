package com.github.aiderpmsi.pimsdriver.odb;

import java.io.IOException;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class DocDbConnectionFactory {

	public final static String location = "local:/tmp/db/odb";

	public final static String user = "admin";
	
	public final static String pwd = "admin";
	
	protected DocDbConnectionFactory() {
		// This is a singleton
	}

	public static ODatabaseDocumentTx getConnection() throws IOException {
		ODatabaseDocumentTx db = new ODatabaseDocumentTx(location);
		if (!db.exists()) {
			// IF IT IS NOT EXISTING, CREATE IT
			db.create();
			// POPULATE THE CLASSES TYPES
			db.getMetadata().getSchema().createClass("PmsiUpload");
		} else {
			db.open(user, pwd);
		}
		return db;
	}

}
