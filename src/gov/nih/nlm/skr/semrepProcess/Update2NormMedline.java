package gov.nih.nlm.skr.semrepProcess;

public class Update2NormMedline {
    static public void main(String argv[]) {
	DocumentParsing dp = DocumentParsing.getInstance();
	dp.update2NormMedlineDir2DB(argv[0], argv[1], argv[2], argv[3]);
    }
}
