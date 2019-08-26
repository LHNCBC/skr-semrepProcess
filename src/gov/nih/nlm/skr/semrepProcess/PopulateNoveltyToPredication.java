package gov.nih.nlm.skr.semrepProcess;

public class PopulateNoveltyToPredication {

    static public void main(String argv[]) {
	Database db = new Database();
	int startOffset = Integer.parseInt(argv[0]);
	int limit = Integer.parseInt(argv[1]);
	db.populateNoveltyToPredication(startOffset, limit);
    }
}
