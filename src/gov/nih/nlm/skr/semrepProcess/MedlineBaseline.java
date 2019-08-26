package gov.nih.nlm.skr.semrepProcess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MedlineBaseline {
    private static final int MAX_RETURN = 1000000;
    private static MedlineBaseline myInstance;
    private static Database database = null;
    static final String selectString = "select PMID from FACT_DATA where PMID = \"";
    static final String insertString = "insert into FACT_DATA (PMID, TITLE, ABSTRACT) VALUES (\"";
    static final String insert2String = "insert into FACT_DATA (PMID, TITLE) VALUES (\"";
    static final String updateString = "update FACT_DATA set TITLE = \"";

    private MedlineBaseline() {
    }

    public static MedlineBaseline getInstance() {
	if (myInstance == null)
	    synchronized (MedlineBaseline.class) {
		if (myInstance == null)
		    myInstance = new MedlineBaseline();
	    }
	return myInstance;
    }

    public List<PubmedArticle> extractMetaInfo(File file)
	    throws SAXException, ParserConfigurationException, IOException {
	SAXParserFactory factory = SAXParserFactory.newInstance();
	SAXParser saxParser = factory.newSAXParser();
	MedlineBaselineParser handler = new MedlineBaselineParser();
	saxParser.parse(file, handler);
	return handler.getArticles();
    }

    public List<PubmedArticle> extractCitationInfo(File file)
	    throws SAXException, ParserConfigurationException, IOException {
	SAXParserFactory factory = SAXParserFactory.newInstance();
	SAXParser saxParser = factory.newSAXParser();

	PubMedArticleParser handler = new PubMedArticleParser();
	Reader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
	InputSource is = new InputSource();
	is.setCharacterStream(isr);
	saxParser.parse(is, handler);

	return handler.getArticles();
    }

    public void convertXML2Text(String infileName, String outfileName, Database db)
	    throws SAXException, ParserConfigurationException, IOException {

	PrintWriter pw = new PrintWriter(outfileName);
	File inFile = new File(infileName);
	List<PubmedArticle> articles = extractCitationInfo(inFile);

	for (PubmedArticle article : articles) {
	    StringBuffer sb = new StringBuffer();
	    sb.append(article.getPMID() + "\n");
	    sb.append(article.getTitle() + "\n");
	    sb.append(article.getAbstract() + "\n\n");
	    pw.println(sb.toString());
	    pw.flush();
	    db.insertTitleAbstract(article.getPMID(), article.getTitle(), article.getAbstract());
	}
	pw.close();

    }

    public void updateXML2Abstract(String infileName, String outfileName, Database db, PrintWriter pw)
	    throws SAXException, ParserConfigurationException, IOException {

	File inFile = new File(infileName);
	List<PubmedArticle> articles = extractCitationInfo(inFile);

	for (PubmedArticle article : articles) {
	    StringBuffer sb = new StringBuffer();
	    sb.append(article.getPMID() + "\n");
	    sb.append(article.getTitle() + "\n");
	    sb.append(article.getAbstract() + "\n\n");
	    // pw.println(sb.toString());
	    // pw.flush();
	    if (article.numAbstractText > 1) {
		pw.println(article.getPMID());
		pw.flush();
		db.updateAbstract(article.getPMID(), article.getAbstract());
	    }
	}
	pw.close();

    }

    public void convertXML2Medline(String infileName, String outfileName)
	    throws SAXException, ParserConfigurationException, IOException {
	File infile = new File(infileName);
	File outfile = new File(outfileName);
	PrintWriter pw = new PrintWriter(outfile);
	List<PubmedArticle> articles = extractCitationInfo(infile);

	for (PubmedArticle article : articles) {
	    StringBuffer sb = new StringBuffer();
	    sb.append("PMID- " + article.getPMID() + "\n");
	    sb.append("TI  - " + replace_UTF8.ReplaceLooklike(article.getTitle()) + "\n");
	    if (article.getAbstract() != null) {
		sb.append("AB  - " + replace_UTF8.ReplaceLooklike(article.getAbstract()) + "\n\n");
	    } else
		sb.append("\n");
	    pw.println(sb.toString());
	    pw.flush();
	}
	pw.close();
    }

    /*
     * Adding Title and Abstract to Factuality database
     */
    public void convertXML2Medline(String infileName, String outfileName, Database db)
	    throws SAXException, ParserConfigurationException, IOException {
	File infile = new File(infileName);
	File outfile = new File(outfileName);
	PrintWriter pw = new PrintWriter(outfile);
	List<PubmedArticle> articles = extractCitationInfo(infile);

	for (PubmedArticle article : articles) {
	    StringBuffer sb = new StringBuffer();
	    sb.append("PMID- " + article.getPMID() + "\n");
	    sb.append("TI  - " + replace_UTF8.ReplaceLooklike(article.getTitle()) + "\n");
	    if (article.getAbstract() != null) {
		sb.append("AB  - " + replace_UTF8.ReplaceLooklike(article.getAbstract()) + "\n\n");
	    } else
		sb.append("\n");
	    pw.println(sb.toString());
	    pw.flush();
	    db.insertTitleAbstract(article.getPMID(), article.getTitle(), article.getAbstract());
	}
	pw.close();
    }
}
