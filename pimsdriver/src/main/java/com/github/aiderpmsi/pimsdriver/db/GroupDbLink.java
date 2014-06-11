package com.github.aiderpmsi.pimsdriver.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import com.github.aiderpmsi.pims.grouper.model.RssActe;
import com.github.aiderpmsi.pims.grouper.model.RssContent;
import com.github.aiderpmsi.pims.grouper.model.RssDa;
import com.github.aiderpmsi.pims.grouper.model.RssMain;
import com.github.aiderpmsi.pims.grouper.tags.Group;
import com.github.aiderpmsi.pims.grouper.utils.Grouper;
import com.github.aiderpmsi.pims.treebrowser.TreeBrowserException;

/**
 * Process makeing the calls to database while main process reads pmsi file
 * @author jpc
 *
 */
public class GroupDbLink extends InputStream implements Callable<Path> {

	/** Queue serving as an ipc */
	public LinkedBlockingQueue<GroupEntry> queue = new LinkedBlockingQueue<>(2048);

	/** Buffer for readed elements remaining */
	private byte[] remaining = {}; 

	/** Indicates if the end element has been reached */
	protected boolean end = false;
	
	/** Temp file used */
	private Path tmpFile;
	
	/** Rss (list of Rums) */
	private List<RssContent> fullRss = new ArrayList<>();
	
	/** List of pmsipositions of current rss */
	private List<Long> pmsiPositions = new ArrayList<>();
	
	/** Last rss number */
	private String lastNumRss = null;
	
	/** Used Grouper */
	private Grouper grouper;
		
	/** Positino in pmsi */
	protected Long pmsiPosition;
	
	public GroupDbLink(Long startPmsiPosition) throws IOException {
		this.pmsiPosition = startPmsiPosition;
		// USE A TEMP FILE TO STORE GROUPING RESULTS
		tmpFile = Files.createTempFile("", "");
		// GROUPER USED
		try {
			grouper = new Grouper();
		} catch (TreeBrowserException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Path call() throws InterruptedException, IOException {
		// COPY FROM THIS READER TO THE FILE
		Files.copy(this, tmpFile, StandardCopyOption.REPLACE_EXISTING);
		
		if (Thread.interrupted())
			throw new InterruptedException();
		
		return tmpFile;
	}

	@Override
	public int read() throws IOException {
		if (end)
			return -1;
		
		boolean append = true;
		if (remaining.length == 0)
			append = fillBuffer();
		
		if (append == true) {
			byte byt = remaining[0];
			remaining = Arrays.copyOfRange(remaining, 1, remaining.length);
			return (int) byt;
		} else {
			return -1;
		}
	}
	
	@Override
	public int read(byte[] buf, int off, int len) throws java.io.IOException {
		// WE ARE OUT OF STREAM
		if (end)
			return -1;

		while (remaining.length < len && fillBuffer() == true) {
			// LOOP (ACTION IS IN FILLBUFFER)
		}
		
		if (remaining.length > len) {
			System.arraycopy(remaining, 0, buf, off, len);
			remaining = Arrays.copyOfRange(remaining, len, remaining.length);
			return len;
		} else if (remaining.length == 0) {
			return -1;
		} else {
			System.arraycopy(remaining, 0, buf, off, remaining.length);
			return remaining.length;
		}
	}
		
	public void store(GroupEntry groupEntry) throws InterruptedException {
		while (!queue.offer(groupEntry, 1, TimeUnit.SECONDS)) {
			if (Thread.interrupted())
				throw new InterruptedException();
		}
	}

	@Override
	public void close() throws IOException {
		// DO NOTHING
	};
	
	private void escape(CharSequence sgt, StringBuilder append) {		
		int size = sgt.length();
		for (int i = 0 ; i < size ; i++) {
			char character = sgt.charAt(i); 
			if (character == '\\')
				append.append(escapeEscape);
			else if (character == '|')
				append.append(escapeDelim);
			else
				append.append(character);
		}
	}
	
	/**
	 * 
	 * @return true if possible to add something, false if impossible
	 * @throws IOException
	 */
	private boolean fillBuffer() throws IOException {
		// INITIAL BUFFER SIZE
		int initialSize = remaining.length;
		// INDICATES IF SOMETHING HAS REALLY BEEN INSERTED
		boolean inserted = false;
		// TRY TO FILL
		while (remaining.length == initialSize && end == false) {
			GroupEntry groupEntry;
			try {
				groupEntry = queue.poll(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// WE ARE INTERRUPTED, STOP READING AND WRITING
				end = true;
				inserted = false;
				break;
			}
			
			if (groupEntry == null) {
				continue;
			} else if (groupEntry.finished) {
				// WE ARE AT END OF STREAM, STORE BUFFER IF NEEDED
				end = true;
				if (fullRss.size() != 0)
					store(fullRss, pmsiPositions);
				inserted = true;
				break;
			}

			// WE HAVE A NEW GROUPENTRY
			// WE HAVE TO STORE THE PRECEDENT FULLRSS IF :
			// 1 - THIS LINE IS A RSSMAIN
			// 2 - PRECEDENT RSS IS DEFINED
			// 3 - THIS LINE HAS A DIFFERENT RSSNUMBER THAN PRECEDENT
			String newNumRss;
			if (groupEntry.line_type.equals("rssmain")
					&& lastNumRss != null
					&& (newNumRss = groupEntry.content.get("NumRSS")) != null
					&& newNumRss != lastNumRss) {
				store(fullRss, pmsiPositions);
				// REINIT FULL RSS
				fullRss = new ArrayList<>();
				pmsiPositions = new ArrayList<>();
				inserted = true;
			}
			
			// DEPENDING ON THE LINE TYPE :
			// 1 - IF IT IS A RSSMAIN, CREATE A NEW RSSCONTENT IN FULLRSS
			// 2 - IF IT IS A RSSACTE OR RSSDA, JUST INSERT THE DATAS TO CURRENT FULLRSS
			if (groupEntry.line_type.equals("rssmain")) {
				RssContent newContent = new RssContent();

				EnumMap<RssMain, String> mainContent = new EnumMap<>(RssMain.class);
				mainContent.put(RssMain.nbseances, groupEntry.content.get("NbSeances"));
				mainContent.put(RssMain.dp, groupEntry.content.get("DP"));
				mainContent.put(RssMain.dr, groupEntry.content.get("DR"));
				mainContent.put(RssMain.modeentree, groupEntry.content.get("ModeEntree"));
				mainContent.put(RssMain.modesortie, groupEntry.content.get("ModeSortie"));
				mainContent.put(RssMain.poidsnouveaune, groupEntry.content.get("PoidsNouveauNe"));
				mainContent.put(RssMain.sexe, groupEntry.content.get("Sexe"));
				mainContent.put(RssMain.dateentree, groupEntry.content.get("DateEntree"));
				mainContent.put(RssMain.datesortie, groupEntry.content.get("DateSortie"));
				mainContent.put(RssMain.ddn, groupEntry.content.get("DDN"));
				mainContent.put(RssMain.agegestationnel, groupEntry.content.get("AgeGestationnel"));

				newContent.setRssmain(mainContent);
				fullRss.add(newContent);
				lastNumRss = groupEntry.content.get("NumRSS");
				pmsiPositions.add(pmsiPosition++);
			} else if (groupEntry.line_type.equals("rssacte")) {
				EnumMap<RssActe, String> acteRss = new EnumMap<>(RssActe.class);
				acteRss.put(RssActe.activite, groupEntry.content.get("Activite"));
				acteRss.put(RssActe.codeccam, groupEntry.content.get("CodeCCAM"));
				acteRss.put(RssActe.phase, groupEntry.content.get("Phase"));
				
				fullRss.get(fullRss.size() - 1).getRssacte().add(acteRss);
				pmsiPositions.add(pmsiPosition++);
			} else if (groupEntry.line_type.equals("rssda")) {
				EnumMap<RssDa, String> daRss = new EnumMap<>(RssDa.class);
				daRss.put(RssDa.da, groupEntry.content.get("DA"));
				
				fullRss.get(fullRss.size() - 1).getRssda().add(daRss);
				pmsiPositions.add(pmsiPosition++);
			}
		}
		return inserted;
	}
	
	private void store(List<RssContent> rums, List<Long> pmsiPositions) throws IOException {
		Group group;
		try {
			group = grouper.group(rums);
			StringBuilder line = new StringBuilder();
			escape(group.getRacine(), line);
			line.append('|');
			escape(group.getModalite(), line);
			line.append('|');
			escape(group.getGravite(), line);
			line.append('|');
			escape(group.getErreur(), line);
			line.append('\n');
			String postfix = line.toString();
			for (Long pmsiPosition : pmsiPositions) {
				remaining = ArrayUtils.addAll(remaining, (Long.toString(pmsiPosition) + '|' + postfix).getBytes("UTF-8"));
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private static final char[] escapeEscape = {'\\', '\\'};

	private static final char[] escapeDelim = {'\\', '|'};

}
