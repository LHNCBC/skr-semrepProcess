package gov.nih.nlm.skr.semrepProcess;

public class UpdateToASCIIFromDB {
    public static void main(String[] args) {
	Database db = new Database();
	db.updateToASCIITitleAbstractFromSemRepZero();
    }
}
