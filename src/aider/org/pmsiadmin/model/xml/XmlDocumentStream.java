package aider.org.pmsiadmin.model.xml;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;


public class XmlDocumentStream {

	private String input;
	
	public XmlDocumentStream(String input) {
		this.input = input;
	}
	
	public void saveTo(XMLStreamWriter streamWriter) throws XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();		
		XMLEventReader eventReader = factory.createXMLEventReader(
				new ByteArrayInputStream(input.getBytes()),
				"UTF-8");

		try {
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					streamWriter.writeStartElement(startElement.getName().getLocalPart());
					@SuppressWarnings("unchecked")
					Iterator<Attribute> it = startElement.getAttributes();
					while (it.hasNext()) {
						Attribute att = it.next();
						streamWriter.writeAttribute(att.getName().getLocalPart(), att.getValue());
					}
				} else if (event.isEndElement()) {
					streamWriter.writeEndElement();
				} else if (event.isCharacters()) {
					Characters characters = event.asCharacters();
					streamWriter.writeCharacters(characters.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
}
