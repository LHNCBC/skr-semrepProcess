package gov.nih.nlm.skr.semrepProcess;

public class ExtractNormalizedFromDBToFile {

    static public void main(String argv[]) {
	Database db = new Database();
	int limit = Integer.parseInt(argv[0]);
	int start = Integer.parseInt(argv[1]);

	db.parsingFromDBSourceToFile(limit, start, argv[2]);
    }
}
