package gov.nih.nlm.skr.semrepProcess;

public class SemreppingAddition {
    static public void main(String argv[]) {
	DocumentParsing dp = new DocumentParsing();
	String orgXMLDir = argv[0];
	String ASCIIXMLDir = argv[1];
	String medlineDir = argv[2];
	String semrepDir = argv[3];
	dp.semreppingAdditionDir2DB(orgXMLDir, ASCIIXMLDir, medlineDir, semrepDir);
	// dp.semrepping2DB(medlinefileName, semrepfileName);
    }
}
