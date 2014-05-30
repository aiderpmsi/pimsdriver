package com.github.aiderpmsi.pimsdriver.db;

import java.util.Iterator;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public abstract class ContentHandlerHelper implements ContentHandler {

	protected LinkedList<String> contentPath = new LinkedList<>();
	
	@Override
	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
		// REMOVE THE LAST ELEMENT
		contentPath.removeLast();
	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
		// ADDS THE ELEMENT
		contentPath.addLast(arg1);
	}

	protected boolean isElement(String[][] eltDef) {
		// IF WE HAVE NOT THE CORRECT NUMBER OF ARGUMENTS, GO AWAY
		if (contentPath.size() != eltDef.length)
			return false;
		
		// CHECK IF THE ELEMENTS ARE ACCEPTED
		Iterator<String> it = contentPath.iterator();
		int i = 0;
		while (it.hasNext()) {
			boolean found = false;
			String element = it.next();
			
			for (int j = 0 ; j < eltDef[i].length ; j++) {
				if (eltDef[i][j].equals("*") || element.equals(eltDef[i][j])) {
					found = true;
					break;
				}
			}
			
			if (found == false)
				return false;
			
			i++;
		}
		
		// ALL ELEMENTS ARE GOOD
		return true;
	}
}
