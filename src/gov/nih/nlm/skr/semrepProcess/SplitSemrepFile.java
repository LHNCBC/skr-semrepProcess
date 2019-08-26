package gov.nih.nlm.skr.semrepProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class SplitSemrepFile {

    public static void main(String args[]) {
	try {
	    BufferedReader br = new BufferedReader(new FileReader(args[0]));
	    String line = null;
	    StringBuilder sb = new StringBuilder();
	    String curPMID = null;
	    String prevPMID = null;

	    while ((line = br.readLine()) != null) {
		//String[] compo = line.split("\\|");
		if (line.startsWith("SE|")) { // first line that has PMID
		    String[] compo = line.split("\\|");
		    prevPMID = curPMID;
		    curPMID = compo[1];
		    if (prevPMID == null || !curPMID.equals(prevPMID)) {
			if (sb != null && sb.length() > 0) { // If there is previous PMID
			    PrintWriter pw = new PrintWriter(
				    new FileWriter(args[1] + File.separator + prevPMID + ".txt"));
			    pw.println(sb.toString());
			    pw.close();
			    sb = new StringBuilder();
			    sb.append(line + "\n");
			} else
			    sb.append(line + "\n");
		    } else
			sb.append(line + "\n");
		}

	    }

	    if (sb.length() > 0) { // NEED TO CREATE THE LAST pmid
		PrintWriter pw = new PrintWriter(new FileWriter(args[1] + File.separator + curPMID + ".txt"));
		pw.println(sb.toString());
		pw.close();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
