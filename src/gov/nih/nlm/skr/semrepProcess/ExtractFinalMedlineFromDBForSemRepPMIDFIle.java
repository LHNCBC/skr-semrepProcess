package gov.nih.nlm.skr.semrepProcess;

public class ExtractFinalMedlineFromDBForSemRepPMIDFIle {
    static public void main(String argv[]) {
	Database db = new Database();
	String tmpDir = argv[0];
	String pmidfile = argv[1];
	db.extractUnsemreppedCitationsFromDBSourcePMIDFile(tmpDir, pmidfile);
	// db.FindPMIDInCITATIONNotInSENTENCE(argv[0], argv[1]);
	// dp.semrepping2DB(medlinefileName, semrepfileName);
    }
}
