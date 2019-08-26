package gov.nih.nlm.skr.semrepProcess;

public class DatabaseUtil {
    static public void main(String args[]) {
	Database db = new Database();
	db.FindPMIDInFACTDATANotInCITATION(args[0]);
    }
}
