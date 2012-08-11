package aider.org.pmsiadmin.view;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.springframework.web.servlet.View;

import aider.org.pmsi.exceptions.PmsiParserException;
import aider.org.pmsiadmin.model.xml.XmlReport;

public class InsertionPmsiView implements View {
	
	private final static String contentType = "text/xml"; 

	public InsertionPmsiView() {
	}
  
	public String getContentType() {
		return contentType;
	}
  
	@SuppressWarnings("unchecked")
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
		throws XMLStreamException, IOException {
		
		response.setContentType(contentType);
		response.setCharacterEncoding("UTF-8");

		XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter(XMLOutputFactory.newInstance().
				createXMLStreamWriter(response.getOutputStream(), "UTF-8"));

		xmlWriter.writeStartDocument("UTF-8", "1.0");
		
		xmlWriter.writeProcessingInstruction("xml-stylesheet href=\"" + request.getContextPath() + "/static/xsl/InsertionPmsi.xsl\" type=\"text/xsl\"");
		
		xmlWriter.writeStartElement("insertionpmsi");
		xmlWriter.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		xmlWriter.writeAttribute("xsi:noNamespaceSchemaLocation", request.getContextPath() + "/static/xsd/InsertionPmsi.xsd");
		xmlWriter.writeAttribute("basedir", request.getContextPath());

		xmlWriter.writeStartElement("status");
		xmlWriter.writeAttribute("value", ((Boolean) model.get("status")).toString());
		xmlWriter.writeEndElement();
		
		xmlWriter.writeStartElement("listinfos");
		for (PmsiParserException ppe : (List<PmsiParserException>) model.get("parserreport")) {
			xmlWriter.writeStartElement("info");
			
			xmlWriter.writeStartElement("parser");
			xmlWriter.writeCharacters(ppe.getStackTrace()[0].getClassName());
			xmlWriter.writeEndElement();
			
			xmlWriter.writeStartElement("error");
			xmlWriter.writeCharacters(ppe.getMessage());
			xmlWriter.writeEndElement();
			
			xmlWriter.writeEndElement();
		}
		
		if (model.get("xmlreport") != null) {
			XmlReport xmlReport = (XmlReport) model.get("xmlreport");
			xmlWriter.flush();
			response.getOutputStream().write(xmlReport.getReport().getBytes(), 0, xmlReport.getReport().getBytes().length);
		}

		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		
		xmlWriter.close();
	}
}
