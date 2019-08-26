package gov.nih.nlm.skr.semrepProcess;

import java.util.List;

public class LoadingSemRep2DB {
    static Database db = Database.getInstance();

    public static void main(String[] args) {
	try {
	    List<String> files = FileUtils.listFiles(args[0], true, args[1]);
	    int start = 0;
	    int end = 0;
	    if (args.length >= 3) {
		start = Integer.parseInt(args[2]);
		end = Integer.parseInt(args[3]);
		System.out.println("Start = " + start);
		System.out.println("End = " + end);
		int cur = 0;
		for (String file : files) {
		    if (cur < end && cur >= start) {
			System.out.println(file);
			db.loadSemRepToSemMedDB(file);
		    }
		    cur++;
		}
	    } else {
		for (String file : files) {
		    System.out.println(file);
		    db.loadSemRepToSemMedDB(file);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
