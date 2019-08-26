package gov.nih.nlm.skr.semrepProcess;

public class ParsingFromDB {
    static public void main(String argv[]) {
	Database db = new Database();
	int limit = Integer.parseInt(argv[0]);
	int start = Integer.parseInt(argv[1]);
	String tmpDir = argv[2];
	db.parsingFromDBSource(limit, start, tmpDir);
	// dp.semrepping2DB(medlinefileName, semrepfileName);
    }
}
