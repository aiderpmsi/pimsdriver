package com.github.aiderpmsi.pimsdriver.jaxb;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.persistence.jaxb.MarshallerProperties;

public class JsonJaxbConverter<T> {

	private JAXBContext jc;
	
	private Marshaller ms;
	
	private String charset;
	
	public JsonJaxbConverter(Class<T> type) throws JAXBException {
		jc = JAXBContext.newInstance(type);
		Marshaller ms = jc.createMarshaller();
        ms.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        ms.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
        ms.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
		charset = "UTF-8";
	}
	
	public String toString(T object) throws JAXBException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ms.marshal(object, baos);
        try {
        	return baos.toString(charset);
		} catch (UnsupportedEncodingException e) {
			// IMPOSSIBLE TO CATCH
			throw new RuntimeException(e);
		}
	}
}
