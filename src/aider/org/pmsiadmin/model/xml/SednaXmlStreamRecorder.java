package aider.org.pmsiadmin.model.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;
import ru.ispras.sedna.driver.SednaStatement;

public class SednaXmlStreamRecorder implements Callable<Integer> {

	private SednaConnection sednaConnection;
	
	private String docName;
	
	private String collectionName;
	
	private InputStream inputStream;
	
	public SednaXmlStreamRecorder(SednaConnection sednaConnection, String docName,
			String collectionName, InputStream inputStream) {
		this.sednaConnection = sednaConnection;
		this.docName = docName;
		this.collectionName = collectionName;
		this.inputStream = inputStream;
	}
	
	@Override
	public Integer call() throws Exception, DriverException, IOException {
		SednaStatement st = sednaConnection.createStatement();
		try {
			st.loadDocument(inputStream, docName, collectionName);
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

}
