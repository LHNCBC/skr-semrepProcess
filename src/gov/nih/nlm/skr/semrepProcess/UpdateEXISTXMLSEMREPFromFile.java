package gov.nih.nlm.skr.semrepProcess;

import java.util.ArrayList;
import java.util.List;

public class UpdateEXISTXMLSEMREPFromFile {
    MedlineSource ms = MedlineSource.getInstance();
    Database db = new Database();

    static public void main(String[] args) {
	try {

	    List<String> PMIDs = FileUtils.linesFromFile(args[0], "UTF-8");
	    // String dbname = args[2];
	    int batch = Integer.parseInt(args[1]);
	    int count = 0;
	    List<Integer> IDs = new ArrayList();
	    UpdateEXISTXMLSEMREPFromFile RA = new UpdateEXISTXMLSEMREPFromFile();
	    for (String PMID : PMIDs) {
		RA.db.resetEXISTInfo(PMID);
		count++;
	    }
	    System.out.println(count);

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }
}
