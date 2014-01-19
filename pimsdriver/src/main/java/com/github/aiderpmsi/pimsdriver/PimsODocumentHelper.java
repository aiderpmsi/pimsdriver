package com.github.aiderpmsi.pimsdriver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.record.impl.ORecordBytes;

public class PimsODocumentHelper {

	private ODocument odoc;
	
	public PimsODocumentHelper(ODocument odoc) {
		this.odoc = odoc;
	}
	
	public ODocument field(String field, InputStream stream) throws IOException {
		
		// DECLARE MASSIVE INSERT
		odoc.getDatabase().declareIntent(new OIntentMassiveInsert());

		// LIST OF IDS IN ORIENTDB FOR THIS FILE (IN MEMORY)
		List<ORID> chunks = new ArrayList<ORID>();
	
		// OPEN FILE
		BufferedInputStream bstream = 
				new BufferedInputStream(stream);

		while (bstream.available() > 0) {
			// PREPARE THE RECORD WE WILL INSERT
			final ORecordBytes chunk = new ORecordBytes();

			// READ REMAINING DATA, BUT NOT MORE THAN 8K
			chunk.fromInputStream(bstream, 8192);

			// SAVE THE CHUNK TO GET THE REFERENCE (IDENTITY) AND FREE FROM THE MEMORY
			odoc.getDatabase().save(chunk);

			// SAVE ITS REFERENCE INTO THE COLLECTION
			chunks.add(chunk.getIdentity());
		}

		// SAVE THE COLLECTION OF REFERENCES IN THE FILE ELEMENT
		ODocument ret = odoc.field(field, chunks);

		// STOP MASSIVE INSERT
		odoc.getDatabase().declareIntent(null);
		
		// RETURN THE FIELD ODocument
		return ret;
	}
}
