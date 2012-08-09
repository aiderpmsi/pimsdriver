package aider.org.pmsiadmin.model.xml;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ru.ispras.sedna.driver.SednaConnection;

import aider.org.pmsi.exceptions.PmsiWriterException;
import aider.org.pmsi.parser.linestypes.PmsiLineType;
import aider.org.pmsi.writer.PmsiXmlWriter;

/**
 * Etend l'interface PmsiXmlWriter pour Ecrire les données xml dans Sedna
 * @author delabre
 *
 */
public class PmsiSednaXmlWriter extends PmsiXmlWriter {

	/**
	 * PipedInputStream dans lequel on lit le flux pour le transmettre à sedna
	 */
	private PipedInputStream inputStream = null;
	
	/**
	 * PipedOutputStream dans lequel on écrit le flux pour le transmettre à sedna
	 */
	private PipedOutputStream outputStream = null;

	/**
	 * Executeur de threads
	 */
	private ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
	
	/**
	 * Thread utilisé
	 */
	private Future<Integer> threadExecuted = null;
	
	/**
	 * Date à uiliser
	 */
	private String date = null;
	
	/**
	 * Construction, écrit sur le flux sortant fourni, avec l'encoding désiré
	 * @param outputStream
	 * @param encoding
	 * @throws PmsiWriterException
	 */
	public PmsiSednaXmlWriter() throws PmsiWriterException {
		super();
	}

	public void open(SednaConnection sednaConnection, String docName, String collectionName, String date) throws PmsiWriterException {
		// Défintion des variables d'instance de classe
		this.date = date;
		
		// Création des flux connectés
		try {
			outputStream = new PipedOutputStream();
			inputStream = new PipedInputStream(outputStream);
		} catch (IOException e) {
			throw new PmsiWriterException(e);
		}
		
		// Transmission de ces flux à la classe parente
		super.open(outputStream, "UTF-8");
		
		// Lancement d'un callable pour transférer le flux
		SednaXmlStreamRecorder recorder = new SednaXmlStreamRecorder(sednaConnection, docName, collectionName, inputStream);
		threadExecuted = threadExecutor.submit(recorder);
	}
	
	@Override
	public void writeStartDocument(String name, String[] attributes, String[] values) throws PmsiWriterException {
		checkFutureFailed(threadExecuted);
		
		String[] newAttributes = new String[attributes.length + 1];
		String[] newValues = new String[attributes.length + 1];
		System.arraycopy(attributes, 0, newAttributes, 0, attributes.length);
		System.arraycopy(values, 0, newValues, 0, attributes.length);

		newAttributes[attributes.length] = "insertionTimeStamp";
		newValues[attributes.length] = date;
		super.writeStartDocument(name, newAttributes, newValues);
	}
	
	@Override
	public void writeStartElement(String name) throws PmsiWriterException {
		checkFutureFailed(threadExecuted);
		super.writeStartElement(name);
	}

	public void writeEndElement() throws PmsiWriterException {
		checkFutureFailed(threadExecuted);
		super.writeEndElement();
	}

	public void writeLineElement(PmsiLineType lineType) throws PmsiWriterException {
		checkFutureFailed(threadExecuted);
		super.writeLineElement(lineType);
	}

	public void writeEndDocument() throws PmsiWriterException {
		checkFutureFailed(threadExecuted);
		super.writeEndDocument();
	}

	
	/**
	 * Libère toutes les ressources associées à ce writer.
	 * Ce n'est qu'à ce moment que l'on sait si l'écriture a été réussie ou non dans la db
	 * de Sedna.
	 * @throws PmsiWriterException
	 */
	public void close() throws PmsiWriterException {
		// Fermeture des resources de la classe parente
		super.close();
		try {
			// 1 - Fermeture du flux outputStream
			if (outputStream != null) {
				outputStream.close();
				outputStream = null;
			}
			
			// 2 - Attente de la fin du thread d'écriture dans Sedna
			try {
				threadExecuted.get();
			} catch (ExecutionException e) {
				throw (Exception) e.getCause();
			} catch (InterruptedException e) {
				throw e;
			}
		} catch (Exception e) {
			throw new PmsiWriterException(e);
		} finally {
			// 3 - Fermeture de l'inputstream
			try {
				if (inputStream != null) {
					inputStream.close();
					inputStream = null;
				}
			} catch (IOException e) {
				throw new PmsiWriterException(e);
			}
		}
	}
	
	private void checkFutureFailed(Future<?> future) throws PmsiWriterException {
		try {
			future.get(0, TimeUnit.NANOSECONDS);
		} catch (TimeoutException e) {
			return;
		} catch (InterruptedException e) {
			throw new PmsiWriterException(e);
		} catch (ExecutionException e) {
			throw new PmsiWriterException(e);
		}
	}
}
