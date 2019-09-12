package gov.nih.nlm.skr.semrepProcess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

/**
 * @author Dongwook Shin
 *
 */
public class MedlineSource {

    private static final int MAX_RETURN = 1000000;

    private static MedlineSource myInstance;
    static Properties properties;
    static String email;

    private MedlineSource() {
    }

    public static MedlineSource getInstance() {
	if (myInstance == null)
	    synchronized (MedlineSource.class) {
		if (myInstance == null) {
		    try {
			myInstance = new MedlineSource();
			properties = FileUtils.loadPropertiesFromFile("semrep.properties");
			email = properties.getProperty("schedulerEmail");
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    }
	return myInstance;
    }

    public List<PubmedArticle> fetch(List<Integer> ids) throws SAXException, ParserConfigurationException, IOException {
	SAXParserFactory factory = SAXParserFactory.newInstance();
	SAXParser saxParser = factory.newSAXParser();
	PubMedArticleParser handler = new PubMedArticleParser();

	StringBuffer sb = new StringBuffer();
	// System.out.println("ids size = " + ids.size());
	sb.append("id=" + ids.get(0));
	for (int i = 1; i < ids.size(); i++)
	    sb.append("," + ids.get(i));

	System.out.println("List of IDs = " + sb.toString());
	URL url = new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi");
	URLConnection conn = url.openConnection();
	conn.setDoOutput(true);
	OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	wr.write("db=pubmed&tool=semmed&email=" + email + "&retmode=xml&retmax=20000&" + sb.toString());
	wr.flush();
	saxParser.parse(conn.getInputStream(), handler);

	return handler.getArticles();
    }

    public List<PubmedArticle> fetch(Integer ids) throws SAXException, ParserConfigurationException, IOException {
	SAXParserFactory factory = SAXParserFactory.newInstance();
	SAXParser saxParser = factory.newSAXParser();
	PubMedArticleParser handler = new PubMedArticleParser();

	StringBuffer sb = new StringBuffer();

	sb.append("id=" + ids);

	// System.out.println("List of IDs = " + sb.toString());
	URL url = new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi");
	URLConnection conn = url.openConnection();
	conn.setDoOutput(true);
	OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	wr.write("db=pubmed&tool=semmed&email=" + email + "&retmode=xml&retmax=20000&" + sb.toString());
	wr.flush();
	saxParser.parse(conn.getInputStream(), handler);

	return handler.getArticles();
    }

    public void fetch(String inFile, String dbName, String DBusername, String DBpassword)
	    throws SAXException, ParserConfigurationException, IOException {
	String aLine;
	boolean issnFound = false;
	String issn = null;
	BufferedReader in = new BufferedReader(new FileReader(inFile));
	List<PubmedArticle> articles = new ArrayList<>();
	PubmedArticle art = null;
	try {
	    Class.forName("com.mysql.jdbc.Driver");
	    Connection conn = DriverManager.getConnection(
		    "jdbc:mysql://indsrv2.nlm.nih.gov/" + dbName + "?autoReconnect=true", DBusername, DBpassword);
	    Statement stmtCitation = conn.createStatement();
	    String SQLInsertHead = new String("insert into CITATIONS (PMID, ISSN,  DP, EDAT, PYEAR) VALUES (");

	    while ((aLine = in.readLine()) != null) {
		if (aLine.startsWith("PMID-")) {
		    if (art != null) {
			StringBuffer SQLInsert = new StringBuffer(SQLInsertHead);
			// System.out.println(art.PMID +  " , " + art.datePublished);
			SQLInsert.append(art.PMID + ",\"" + art.issn + "\",\"" + art.datePublished + "\",\"" + art.EDAT
				+ "\",\"" + art.PYear + "\")");

			// System.out.println(SQLInsert.toString());
			try {
			    stmtCitation.executeUpdate(SQLInsert.toString());
			    // System.out.println("Pyear = " + art.PYear);
			} catch (Exception e) {
			    e.printStackTrace();
			    // System.out.println("Update PYear for PMID = " + art.PMID + " to " + art.PYear);
			    // stmtCitation2.executeUpdate(SQLAddYear.replace("%1", Integer.toString(art.PYear)).replace("%2", art.PMID));
			}
		    }

		    art = new PubmedArticle();
		    String[] compo = aLine.split(" ");
		    art.setPMID(compo[1].trim());
		} else if (aLine.startsWith("DP  -")) {
		    String dp = aLine.substring(6);
		    art.setDatePublished(dp);
		    String yearStr = dp.substring(0, 4);
		    try {
			int pyear = Integer.parseInt(yearStr);
			art.setPYear(pyear);
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		} else if (aLine.startsWith("EDAT-")) {
		    String[] compo = aLine.split(" ");
		    String edat = compo[1];
		    String yearStr[] = edat.split("/");
		    art.setEDAT(edat);
		    try {
			int pyear = Integer.parseInt(yearStr[0]);
			art.setPYear(pyear);
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		} else if (aLine.startsWith("IS  -")) {
		    int index = aLine.indexOf("(");
		    if (index < 0)
			index = aLine.length() + 1;
		    if (!issnFound) {
			issn = aLine.substring(6, index - 1);
			art.setIssn(issn);
		    }
		}
	    } // while

	    if (art != null) {
		StringBuffer SQLInsert = new StringBuffer(SQLInsertHead);
		// System.out.println(art.PMID +  " , " + art.datePublished);
		SQLInsert.append(art.PMID + ",\"" + art.issn + "\",\"" + art.datePublished + "\",\"" + art.EDAT
			+ "\",\"" + art.PYear + "\")");

		// System.out.println(SQLInsert.toString());
		try {
		    stmtCitation.executeUpdate(SQLInsert.toString());
		    // System.out.println("Pyear = " + art.PYear);
		} catch (Exception e) {
		    e.printStackTrace();
		    // System.out.println("Update PYear for PMID = " + art.PMID + " to " + art.PYear);
		    // stmtCitation2.executeUpdate(SQLAddYear.replace("%1", Integer.toString(art.PYear)).replace("%2", art.PMID));
		}
	    }
	    stmtCitation.close();
	    conn.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
