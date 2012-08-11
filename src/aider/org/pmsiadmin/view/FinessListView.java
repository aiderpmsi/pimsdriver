package aider.org.pmsiadmin.view;

import java.io.IOException;
import java.util.Map;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.springframework.web.servlet.View;

import aider.org.pmsiadmin.model.xml.XmlDocumentStream;


public class FinessListView implements View {
	
	private final static String contentType = "text/xml"; 

	public FinessListView() {
	}
  
	public String getContentType() {
		return contentType;
	}
  
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
		throws XMLStreamException, IOException {
		
		response.setContentType(contentType);
		response.setCharacterEncoding("UTF-8");

		XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter(XMLOutputFactory.newInstance().
				createXMLStreamWriter(response.getOutputStream(), "UTF-8"));

		xmlWriter.writeStartDocument("UTF-8", "1.0");
		
		xmlWriter.writeProcessingInstruction("xml-stylesheet href=\"" + request.getContextPath() + "/static/xsl/FinessList.xsl\" type=\"text/xsl\"");
		
		xmlWriter.writeStartElement("finesslist");
		xmlWriter.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		xmlWriter.writeAttribute("xsi:noNamespaceSchemaLocation", request.getContextPath() + "/static/xsd/FinessList.xsd");
		xmlWriter.writeAttribute("basedir", request.getContextPath());
		
		if (model.get("finess") != null) {
			XmlDocumentStream xmlDocumentStream = new XmlDocumentStream((String) model.get("finess"));
			xmlDocumentStream.saveTo(xmlWriter);
		}

		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();

		xmlWriter.close();
	}
}
