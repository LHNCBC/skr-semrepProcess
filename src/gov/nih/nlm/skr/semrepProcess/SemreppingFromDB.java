package gov.nih.nlm.skr.semrepProcess;

public class SemreppingFromDB {
    static public void main(String argv[]) {
	Database db = new Database();
	int limit = Integer.parseInt(argv[0]);
	int offset = Integer.parseInt(argv[1]);
	String tmpDir = argv[2];
	db.semrepFromDBSource(limit, offset, tmpDir);
	// dp.semrepping2DB(medlinefileName, semrepfileName);
    }
}
