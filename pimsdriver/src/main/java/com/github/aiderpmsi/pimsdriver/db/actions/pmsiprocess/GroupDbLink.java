package com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.github.aiderpmsi.pims.grouper.model.RssActe;
import com.github.aiderpmsi.pims.grouper.model.RssContent;
import com.github.aiderpmsi.pims.grouper.model.RssDa;
import com.github.aiderpmsi.pims.grouper.model.RssMain;
import com.github.aiderpmsi.pims.grouper.utils.Grouper;
import com.github.aiderpmsi.pims.grouper.utils.GrouperFactory;
import com.github.aiderpmsi.pims.treebrowser.TreeBrowserException;

/**
 * Process makeing the calls to database while main process reads pmsi file
 * @author jpc
 *
 */
public class GroupDbLink implements Callable<Path> {

	/** Queue serving as an ipc */
	public LinkedBlockingQueue<GroupEntry> queue = new LinkedBlockingQueue<>(2048);

	/** Temp file used */
	private Path tmpFile;
	
	/** Rss (list of Rums) */
	private List<RssContent> fullRss = new ArrayList<>();
	
	/** Last rss number */
	private String lastNumRss = null;
	
	/** Used Grouper */
	private Grouper grouper;
		
	/** Position in pmsi */
	protected Long pmsiPosition;
	
	/** List of pmsipositions of current rss */
	private List<Long> pmsiPositions = new ArrayList<>();
	
	public GroupDbLink(Long startPmsiPosition) throws IOException {
		this.pmsiPosition = startPmsiPosition;
		// USE A TEMP FILE TO STORE GROUPING RESULTS
		tmpFile = Files.createTempFile("", "");
		// GROUPER USED
		try {
			GrouperFactory gf = new GrouperFactory();
			grouper = gf.newGrouper();
		} catch (TreeBrowserException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Path call() throws InterruptedException, IOException {
		// OPENS THE FILE FOR WRITING
		try (BufferedWriter writer = Files.newBufferedWriter(tmpFile, Charset.forName("UTF-8"), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
		
			for (;;) {
				
				// SEE IF THE THREAD HAS BEEN INTERRUPTED
				if (Thread.interrupted())
					throw new InterruptedException();
				
				// REPEAT UNTIL READING A NEW GROUP ENTRY (LET BE INTERRUPTED IF NEEDED)
				GroupEntry groupEntry;
				while ((groupEntry = queue.poll(1, TimeUnit.SECONDS)) == null) {}
	
				// TRY TO SEE IF A STOP TOKEN HAS BEEN SENT
				if (groupEntry.finished) {
					// WE ARE AT END OF STREAM, FINISHES THE WRITING
					if (fullRss.size() != 0)
						groupAndStore(grouper, writer, fullRss, pmsiPositions);
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
					groupAndStore(grouper, writer, fullRss, pmsiPositions);
					// REINIT FULL RSS
					fullRss = new ArrayList<>();
					pmsiPositions = new ArrayList<>();
				}
				
				// DEPENDING ON THE LINE TYPE :
				// 1 - IF IT IS A RSSMAIN, CREATE A NEW RSSCONTENT IN FULLRSS
				// 2 - IF IT IS A RSSACTE OR RSSDA, JUST INSERT THE DATAS TO CURRENT FULLRSS
				// 3 - IF IT IS A RSSHEADER, INCREMENT PMSIPOSITION
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
					pmsiPositions.add(pmsiPosition);
					pmsiPosition++;
				} else if (groupEntry.line_type.equals("rssacte")) {
					EnumMap<RssActe, String> acteRss = new EnumMap<>(RssActe.class);
					acteRss.put(RssActe.activite, groupEntry.content.get("Activite"));
					acteRss.put(RssActe.codeccam, groupEntry.content.get("CodeCCAM"));
					acteRss.put(RssActe.phase, groupEntry.content.get("Phase"));
					
					fullRss.get(fullRss.size() - 1).getRssacte().add(acteRss);
					pmsiPosition++;
				} else if (groupEntry.line_type.equals("rssda")) {
					EnumMap<RssDa, String> daRss = new EnumMap<>(RssDa.class);
					daRss.put(RssDa.da, groupEntry.content.get("DA"));
					
					fullRss.get(fullRss.size() - 1).getRssda().add(daRss);
					pmsiPosition++;
				} else if (groupEntry.line_type.equals("rssheader")) {
					pmsiPosition++;
				}
			}
			return tmpFile;
		} catch (Exception e) {
			System.out.println(e);
			throw e;
		}
	}
	
	public void store(GroupEntry groupEntry) throws InterruptedException {
		while (!queue.offer(groupEntry, 1, TimeUnit.SECONDS)) {
			if (Thread.interrupted())
				throw new InterruptedException();
		}
	}

	private static void escapeAndAppend(final CharSequence sgt, final StringBuilder append) {		
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
		
	private static void groupAndStore(final Grouper grouper, final BufferedWriter writer,
			final List<RssContent> rums, final List<Long> pmsiPositions) throws IOException {
		HashMap<?, ?> group;
		try {
			// MUST CATCH EVERY EXCEPTIONs (EVEN RUNTIMEEXCEPTION)
			group = grouper.group(rums);
		} catch (Exception e) {
			throw new IOException(e);
		}

		if (group == null) {
			throw new IOException("Groupage result is null, implementation error");
		}
		
		StringBuilder lineBuilder = new StringBuilder();
		escapeAndAppend((String) group.get("racine"), lineBuilder);
		lineBuilder.append('|');
		escapeAndAppend((String) group.get("modalite"), lineBuilder);
		lineBuilder.append('|');
		escapeAndAppend((String) group.get("gravite"), lineBuilder);
		lineBuilder.append('|');
		escapeAndAppend((String) group.get("erreur"), lineBuilder);
		lineBuilder.append('\n');
		String line = lineBuilder.toString();

		// WRITES THE ELEMENT
		for (Long pmsiPosition : pmsiPositions) {
			writer.write(Long.toString(pmsiPosition));
			writer.write('|');
			writer.write(line);
			System.out.println("pmsipos : " + pmsiPosition);
		}
	}

	private static final char[] escapeEscape = {'\\', '\\'};

	private static final char[] escapeDelim = {'\\', '|'};

}
