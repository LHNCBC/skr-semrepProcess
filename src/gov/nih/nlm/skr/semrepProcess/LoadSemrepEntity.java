package gov.nih.nlm.skr.semrepProcess;

import java.util.List;

public class LoadSemrepEntity {
    static public void main(String args[]) {
	Database db = Database.getInstance();
	try {
	    List<String> listSemrepFiles = FileUtils.listFiles(args[0], false, args[1]);
	    int start = Integer.parseInt(args[2]);
	    int end = Integer.parseInt(args[3]);
	    int count = 1;
	    for (String semfileName : listSemrepFiles) {
		if (count >= start && count <= end) {
		    db.loadSemRepEntityToSemMedDB(semfileName);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }
}
