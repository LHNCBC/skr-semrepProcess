package gov.nih.nlm.skr.semrepProcess;

public class PopulateNormilizedSH {
    static public void main(String argv[]) {
	Database db = new Database();
	String normalizeSHTxt = argv[0];
	int startOffset = Integer.parseInt(argv[1]);
	int limit = Integer.parseInt(argv[2]);
	db.populateNormalizedSH2DB(normalizeSHTxt, startOffset, limit);

    }
}
