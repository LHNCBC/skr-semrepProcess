package gov.nih.nlm.skr.semrepProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Metadata {
    static Database db = Database.getInstance();
    static MedlineBaseline mb = MedlineBaseline.getInstance();
    static MedlineSource ms = MedlineSource.getInstance();

    static public void insertMetadata2DB(String ASCIIXMLDir) {
	try {
	    // MedlineBaseline mb = MedlineBaseline.getInstance();
	    List<String> listInXMLFiles = FileUtils.listFiles(ASCIIXMLDir, false, "xml");

	    for (String inFileName : listInXMLFiles) {
		int pos1 = inFileName.lastIndexOf(File.separator) + 1;
		String fileName = inFileName.substring(pos1).replace(".xml", "");
		String XMLFileName = new String(ASCIIXMLDir + File.separator + fileName + ".xml");
		System.out.println(XMLFileName);
		File XMLFile = new File(XMLFileName);
		List<PubmedArticle> articles = mb.extractCitationInfo(XMLFile);
		db.insertMetaData2DB(articles);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    static public void insertMetadata2DBFromFile(String PMIDfile, int batchSize) {
	try {
	    BufferedReader br = new BufferedReader(new FileReader(PMIDfile));
	    List<String> PMIDList = FileUtils.linesFromFile(PMIDfile, "UTF-8");
	    int i = 0;
	    List<Integer> batchList = new ArrayList<>();
	    for (String PMID : PMIDList) {
		if ((i > 0) && ((i % batchSize) == 0)) {
		    List<PubmedArticle> articles = ms.fetch(batchList);
		    db.insertMetaData2DB(articles);
		    batchList = new ArrayList<>();
		} else {
		    batchList.add(Integer.parseInt(PMID));
		}
		i++;
	    }
	    if (batchList.size() > 0) {
		List<PubmedArticle> articles = ms.fetch(batchList);
		db.insertMetaData2DB(articles);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    static public void insertMetadata2DBFromPMID(int PMID) {
	try {
	    int i = 0;
	    List<Integer> batchList = new ArrayList<>();
	    List<PubmedArticle> articles = ms.fetch(PMID);
	    db.insertMetaData2DB(articles);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    static public void main(String[] args) {
	// insertMetadata2DB(args[0]);
	// insertMetadata2DBFromFile("C:\\SemMedDatabase\\version 4.0\\PMIDNotFoundInCitationsTable_2.txt", 500);
	// insertMetadata2DBFromFile(args[0], Integer.parseInt(args[1]));
	insertMetadata2DBFromPMID(20939747);
    }
}
