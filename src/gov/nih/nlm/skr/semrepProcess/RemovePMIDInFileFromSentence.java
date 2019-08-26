package gov.nih.nlm.skr.semrepProcess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class RemovePMIDInFileFromSentence {
    static public void main(String args[]) {
	Database db = new Database();
	try {
	    BufferedReader br = new BufferedReader(new FileReader(args[0]));
	    List<String> lines = FileUtils.linesFromFile(args[0], "ASCII");

	    String PMID = null;
	    for (String line : lines) {
		PMID = line.trim();
		System.out.println(PMID);
		;
		db.removePMIDFromSentenceTable(PMID);

	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
