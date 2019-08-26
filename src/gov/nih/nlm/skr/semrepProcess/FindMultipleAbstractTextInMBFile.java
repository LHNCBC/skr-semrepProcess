package gov.nih.nlm.skr.semrepProcess;

import java.io.PrintWriter;
import java.util.List;

public class FindMultipleAbstractTextInMBFile {
    static public void main(String args[]) {

	try {
	    String file = args[0];
	    int count = 0;
	    PrintWriter pw = new PrintWriter(
		    //	"Q:\\LHC_Projects\\SemrepProcessing\\Ver1.8\\PMIDWithmultipleAbstractText.txt");
		    args[1]);

	    List<String> lines = FileUtils.linesFromFile(file, "ASCII");

	    String PMID = null;
	    for (String line : lines) {
		String lline = line.toLowerCase();
		if (lline.contains("<pubmedarticle")) {
		    count = 0;
		    PMID = null;
		} else if (lline.contains("<pmid")) {
		    if (PMID == null)
			PMID = extractPMID(line);
		} else if (lline.contains("<abstracttext"))
		    count++;
		else if (lline.contains("</pubmedarticle")) {
		    if (count > 1) {
			pw.println(PMID);
			pw.flush();
		    }
		}

	    }
	    pw.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    static String extractPMID(String line) {
	int start = line.indexOf(">");
	start++;
	int end = line.indexOf("</PMID>");
	return line.substring(start, end).trim();
    }
}
