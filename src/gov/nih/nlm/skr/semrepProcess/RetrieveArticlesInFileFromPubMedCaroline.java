package gov.nih.nlm.skr.semrepProcess;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class RetrieveArticlesInFileFromPubMedCaroline {
    MedlineSource ms = MedlineSource.getInstance();

    static public void main(String[] args) {
	try {

	    List<String> PMIDs = FileUtils.linesFromFile(args[0], "UTF-8");
	    int batch = Integer.parseInt(args[1]);
	    PrintWriter pw = new PrintWriter(args[2]);
	    // String dbname = args[2];
	    int count = 0;
	    List<Integer> IDs = new ArrayList();
	    RetrieveArticlesInFileFromPubMedCaroline RA = new RetrieveArticlesInFileFromPubMedCaroline();
	    for (String PMID : PMIDs) {
		if (count > 0 && (count % batch) == 0) {
		    RA.addCitationInfoToFile(IDs, pw);
		    IDs = new ArrayList();
		}
		IDs.add(Integer.parseInt(PMID));
		count++;
	    }
	    if (IDs.size() > 0)
		RA.addCitationInfoToFile(IDs, pw);
	    pw.close();
	} catch (

	Exception e) {
	    e.printStackTrace();
	}

    }

    public void addCitationInfoToFile(List<Integer> ids, PrintWriter pw) {
	try {
	    List<PubmedArticle> articles = ms.fetch(ids);
	    for (PubmedArticle pa : articles) {
		String PMID = pa.getPMID();
		String abs = replace_UTF8.ReplaceLooklike(pa.getAbstract());
		String ti = replace_UTF8.ReplaceLooklike(pa.getTitle());
		// System.out.println(PMID + "\n Abstrat:\n" + abs);
		if (ti != null && ti.length() > 0) {
		    pw.println("PMID- " + PMID);
		    pw.println("TI  - " + ti);
		    if (abs != null && abs.length() > 0)
			pw.println(abs);
		    pw.println();
		}
	    }
	    pw.flush();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
