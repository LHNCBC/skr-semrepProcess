package gov.nih.nlm.skr.semrepProcess;

public class ExtractFinalMedlineFromDBForSemRep {
    static public void main(String argv[]) {
	Database db = new Database();
	String tmpDir = argv[0];
	db.extractUnsemreppedCitationsFromDBSource(tmpDir);
	// db.FindPMIDInCITATIONNotInSENTENCE(argv[0], argv[1]);
	// dp.semrepping2DB(medlinefileName, semrepfileName);
    }
}
