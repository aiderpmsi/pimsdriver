package com.github.aiderpmsi.pimsdriver.pmsi;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class DefaultContentHandler implements ContentHandler {

    /**
* Writer utilisé dans ce collecteur
*/
    private Writer out;

    /**
* Constructeur
*/
    public DefaultContentHandler(Writer writer) {
            this.out = writer;
    }

    // ======== Méthodes pour ContentHandler =======
    public void setDocumentLocator(Locator locator) {
            // Do nothing
    }

    public void startDocument() throws SAXException {
            try {
                    out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
                    out.flush();
            } catch (IOException e) {
                    throw new SAXException(e);
            }
    }

    public void endDocument() throws SAXException {
            try {
                    out.flush();
            } catch (IOException e) {
                    throw new SAXException(e);
            }
    }

    public void startPrefixMapping(String prefix, String uri)
                    throws SAXException {
            // Do nothing
    }

    public void endPrefixMapping(String prefix) throws SAXException {
            // Do nothing
    }

    public void startElement(String uri, String localName, String qName,
                    Attributes atts) throws SAXException {
            StringBuilder builder = new StringBuilder();
            try {
                    builder.append("<").append(StringEscapeUtils.escapeXml(localName));
                    if (atts != null) {
                            int i = 0;
                            while (i < atts.getLength()) {
                                    builder.append(" ")
                                                    .append(StringEscapeUtils.escapeXml(atts
                                                                    .getLocalName(i)))
                                                    .append("=\"")
                                                    .append(StringEscapeUtils.escapeXml(atts
                                                                    .getValue(i))).append("\"");
                                    i++;
                            }
                    }
                    builder.append(">");
                    out.write(builder.toString());
                    out.flush();
            } catch (IOException e) {
                    throw new SAXException(e);
            }
    }

    public void endElement(String uri, String localName, String qName)
                    throws SAXException {
            StringBuilder builder = new StringBuilder();
            try {
                    builder.append("</").append(StringEscapeUtils.escapeXml(localName))
                                    .append(">");
                    out.write(builder.toString());
                    out.flush();
            } catch (IOException e) {
                    throw new SAXException(e);
            }
    }

    public void characters(char[] ch, int start, int length)
                    throws SAXException {
            try {
                    out.write(StringEscapeUtils
                                    .escapeXml(new String(ch, start, length)));
                    out.flush();
            } catch (IOException e) {
                    throw new SAXException(e);
            }
    }

    
    public void ignorableWhitespace(char[] ch, int start, int length)
                    throws SAXException {
            // Do nothing
    }

    public void processingInstruction(String target, String data)
                    throws SAXException {
            // Do nothing
    }

    public void skippedEntity(String name) throws SAXException {
            // Do nothing
    }

}