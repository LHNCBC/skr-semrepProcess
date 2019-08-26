package gov.nih.nlm.skr.semrepProcess;

public class PopulateNormilizedSHWithFile {
    static public void main(String argv[]) {
	Database db = new Database();
	String normalizeSHTxt = argv[0];
	String pmidFile = argv[1];
	db.populateNormalizedSH2DBFile(normalizeSHTxt, pmidFile);

    }
}
