package gov.nih.nlm.skr.semrepProcess;

import java.util.ArrayList;
import java.util.List;

public class RetrieveArticlesInFileFromPubMed {
    MedlineSource ms = MedlineSource.getInstance();
    Database db = new Database();

    static public void main(String[] args) {
	try {

	    List<String> PMIDs = FileUtils.linesFromFile(args[0], "UTF-8");
	    int batch = Integer.parseInt(args[1]);
	    // String dbname = args[2];
	    int count = 0;
	    List<Integer> IDs = new ArrayList();
	    RetrieveArticlesInFileFromPubMed RA = new RetrieveArticlesInFileFromPubMed();
	    for (String PMID : PMIDs) {
		if (count > 0 && (count % batch) == 0) {
		    RA.addCitationInfoToDB(IDs);
		    IDs = new ArrayList();
		}
		IDs.add(Integer.parseInt(PMID));
		count++;
	    }
	    if (IDs.size() > 0)
		RA.addCitationInfoToDB(IDs);
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void addCitationInfoToDB(List<Integer> ids) {
	try {
	    List<PubmedArticle> articles = ms.fetch(ids);
	    for (PubmedArticle pa : articles) {
		String PMID = pa.getPMID();
		String abs = replace_UTF8.ReplaceLooklike(pa.getAbstract());
		String ti = replace_UTF8.ReplaceLooklike(pa.getTitle());
		// System.out.println(PMID + "\n Abstrat:\n" + abs);
		if (ti != null && ti.length() > 0)
		    db.updateTitleAbstract(PMID, ti, abs);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
