package gov.nih.nlm.skr.semrepProcess;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExtractWrongMA {
    static public void main(String args[]) {

	try {
	    List<String> lines1 = FileUtils.linesFromFile(args[0], "ASCII");
	    int count = 0;
	    List<String> lines2 = FileUtils.linesFromFile(args[1], "ASCII");
	    PrintWriter pw = new PrintWriter(
		    //	"Q:\\LHC_Projects\\SemrepProcessing\\Ver1.8\\PMIDWithmultipleAbstractText.txt");
		    args[2]);
	    Set<String> firstSet = new HashSet<>();

	    for (String line : lines1) {
		firstSet.add(line.trim());
	    }

	    for (String line : lines2) {
		String newline = line.trim();
		if (!firstSet.contains(newline)) {
		    pw.println(newline);
		    pw.flush();
		}

	    }
	    pw.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
