package gov.nih.nlm.skr.semrepProcess;

import java.io.File;
import java.util.List;

public class testParser {
    static MedlineBaseline mb = MedlineBaseline.getInstance();
    static SchedulerBatchProcessing sbp = SchedulerBatchProcessing.getInstance();
    static Database db = Database.getInstance();

    static public void main(String argv[]) {
	System.out.println("Processing " + argv[0]);
	try {
	    File XMLFile = new File(argv[0]);
	    List<PubmedArticle> articles = mb.extractCitationInfo(XMLFile);
	    db.insertMetaData2DB(articles);
	} catch (Exception e) {

	}
    }
}
