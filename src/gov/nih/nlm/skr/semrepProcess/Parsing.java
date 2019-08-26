package gov.nih.nlm.skr.semrepProcess;

public class Parsing {
    static public void main(String argv[]) {
	DocumentParsing dp = new DocumentParsing();
	String normDir = argv[0];
	String parseDir = argv[1];
	dp.parsingDir2DB(normDir, parseDir);
	// dp.semrepping2DB(medlinefileName, semrepfileName);
    }
}
