package gov.nih.nlm.skr.semrepProcess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

public class SemRep2Database {
    static Properties properties;
    Connection factconn;
    Statement factstmt;
    Connection semmedconn;
    Statement semmedstmt;
    static final String selectString = "select XML_DATA, SEMREP_DATA from FACT_DATA where PMID = \"";
    static final String insertTitleString = "insert into FACT_DATA (PMID, TITLE) VALUES (\"";
    static final String insertTitleAbstractString = "insert into FACT_DATA (PMID, TITLE, ABSTRACT) VALUES (\"";
    static final String insertXMLString = "insert into FACT_DATA (PMID, EXIST_XML, XML_DATA) VALUES (\"";
    static final String updateXMLString = "update FACT_DATA set EXIST_XML = 1, XML_DATA = \""; // $$$$$1\" where PMID =\"$$$$$2\"";
    static final String insertSemRepString = "insert into FACT_DATA (PMID, EXIST_SEMREP, SEMREP_DATA) VALUES (\"";
    static final String updateSemRepString = "update FACT_DATA set EXIST_SEMREP = 1, SEMREP_DATA =  \""; // $$$$$1\" where PMID =\"$$$$$2\"";

    static final String insertMetaString = "insert into CITATIONS (PMID, ISSN, DP, EDAT, PYEAR) VALUES (\"";
    private static SemRep2Database myInstance;

    static String factdbName;
    static String semmeddbName;
    static String dbusername;
    static String dbpassword;
    static String perlScript;
    static String semrepLoadingProgram;
    static String normFile;

    public SemRep2Database() {
	try {
	    properties = FileUtils.loadPropertiesFromFile("semrep.properties");
	    factdbName = properties.getProperty("factdatabase");
	    semmeddbName = properties.getProperty("semmeddatabase");
	    dbusername = properties.getProperty("dbusername");
	    dbpassword = properties.getProperty("dbpassword");
	    semrepLoadingProgram = properties.getProperty("semrepLoadingProgram");
	    perlScript = properties.getProperty("perlScript");
	    normFile = properties.getProperty("semrepNormFile");
	    Class.forName("com.mysql.jdbc.Driver");
	    factconn = DriverManager.getConnection(
		    "jdbc:mysql://indsrv2.nlm.nih.gov/" + factdbName + "?autoReconnect=true", dbusername, dbpassword);
	    factstmt = factconn.createStatement();
	    semmedconn = DriverManager.getConnection(
		    "jdbc:mysql://indsrv2.nlm.nih.gov/" + semmeddbName + "?autoReconnect=true", dbusername, dbpassword);
	    semmedstmt = semmedconn.createStatement();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static SemRep2Database getInstance() {
	if (myInstance == null)
	    synchronized (SemRep2Database.class) {
		if (myInstance == null)
		    myInstance = new SemRep2Database();
	    }
	return myInstance;
    }

    public void saveXmlToDatabase(String file) {
	try {
	    BufferedReader br = new BufferedReader(new FileReader(file));
	    String line = null;
	    String PMID = null;
	    String prevPMID = null;
	    StringBuffer docSB = new StringBuffer();
	    int cnt = 0;
	    while ((line = br.readLine()) != null) {
		if (line.startsWith("<document id=")) {
		    if (docSB.length() > 10) {
			String curSelect = selectString + PMID + "\"";
			ResultSet rs = factstmt.executeQuery(curSelect);
			String xmlstr = docSB.toString();
			xmlstr = xmlstr.replaceAll("\\\\", "\\\\\\\\");
			xmlstr = xmlstr.replaceAll("\"", "\\\\\"");
			if (rs.next()) { // update the row
			    String curUpdateXMLString = new String(
				    updateXMLString + xmlstr + "\" where PMID =\"" + PMID + "\"");
			    factstmt.executeUpdate(curUpdateXMLString);
			    cnt++;
			    // System.out.println(PMID + " : " + cnt);
			} else { // insert the row
			    String curInsertXMLString = new String(
				    insertXMLString + PMID + "\", 1, \"" + xmlstr + "\")");
			    factstmt.executeUpdate(curInsertXMLString);
			}
			docSB = new StringBuffer();
		    }
		    prevPMID = PMID;
		    PMID = extractPMID(line);

		    docSB.append(line + "\n");

		} else {
		    docSB.append(line + "\n"); // append the line to the String Buffer
		}
	    }
	    if (docSB.length() > 10) {
		if (PMID.length() > 0) { // PMID should not be empty string
		    String curSelect = selectString + PMID + "\"";
		    ResultSet rs = factstmt.executeQuery(curSelect);
		    String xmlstr = docSB.toString();
		    xmlstr = xmlstr.replaceAll("\\\\", "\\\\\\\\");
		    xmlstr = xmlstr.replaceAll("\"", "\\\\\"");
		    if (rs.next()) { // update the row
			String curUpdateXMLString = new String(
				updateXMLString + xmlstr + "\" where PMID =\"" + PMID + "\"");
			factstmt.executeUpdate(curUpdateXMLString);
		    } else { // insert the row
			String curInsertXMLString = new String(insertXMLString + PMID + "\", 1, \"" + xmlstr + "\")");
			factstmt.executeUpdate(curInsertXMLString);
		    }
		}
		docSB = new StringBuffer();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void saveSemRepToDatabase(String file) {
	try {
	    BufferedReader br = new BufferedReader(new FileReader(file));
	    String line = null;
	    String PMID = null;
	    String prevPMID = null;
	    StringBuffer sb = new StringBuffer();
	    while ((line = br.readLine()) != null) {
		if (line.startsWith("SE|")) {
		    String compo[] = line.split("\\|");
		    prevPMID = PMID;
		    PMID = compo[1];
		    if (prevPMID != null && !prevPMID.equals(PMID)) {
			String curSelect = selectString + prevPMID + "\"";
			ResultSet rs = factstmt.executeQuery(curSelect);

			String semrepstr = sb.toString();
			semrepstr = semrepstr.replaceAll("\\\\", "\\\\\\\\");
			semrepstr = semrepstr.replaceAll("\"", "\\\\\"");

			if (rs.next()) { // update the row
			    String curUpdateSemRepString = updateSemRepString + semrepstr + "\" where PMID =\""
				    + prevPMID + "\"";
			    factstmt.executeUpdate(curUpdateSemRepString);
			} else { // insert the row
			    String curInsertSemRepString = new String(
				    insertSemRepString + prevPMID + "\", 1, \"" + semrepstr + "\")");
			    factstmt.executeUpdate(curInsertSemRepString);
			}
			sb = new StringBuffer();
		    }

		    sb.append(line + "\n");

		} else {
		    sb.append(line + "\n"); // append the line to the String Buffer
		}
	    }
	    if (sb.length() > 10) {
		String curSelect = selectString + PMID + "\"";
		ResultSet rs = factstmt.executeQuery(curSelect);
		String semrepstr = sb.toString();
		semrepstr = semrepstr.replaceAll("\\\\", "\\\\\\\\");
		semrepstr = semrepstr.replaceAll("\"", "\\\\\"");
		if (rs.next()) { // update the row
		    String curUpdateSemRepString = updateSemRepString + semrepstr + "\" where PMID =\"" + prevPMID
			    + "\"";
		    factstmt.executeUpdate(curUpdateSemRepString);
		} else { // insert the row
		    String curInsertSemRepString = new String(insertXMLString + PMID + "\", 1, \"" + semrepstr + "\")");
		    factstmt.executeUpdate(curInsertSemRepString);
		}
		sb = new StringBuffer();
	    }

	    DatabaseBatch insertDB = new DatabaseBatch();
	    insertDB.setPath(perlScript, semrepLoadingProgram, file, semmeddbName, dbusername, dbpassword, normFile); // if
	    insertDB.start();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public String extractPMID(String line) {
	int index = line.indexOf("preserve\">");
	int startPMID = index + 10;
	int endPMID = line.indexOf(" ", startPMID);
	if (endPMID < 0)
	    endPMID = line.indexOf("\n", startPMID);
	if (endPMID < 0)
	    endPMID = line.length();
	String PMID = line.substring(startPMID, endPMID);
	return PMID;
    }

    public void insertTitleAbstract(String PMID, String title, String abs) {
	String newTitle = title.replaceAll("\\\\", "\\\\\\\\");
	newTitle = newTitle.replaceAll("\"", "\\\\\"");
	String newAbs = null;
	try {
	    String curSelect = selectString + PMID + "\"";
	    ResultSet rs = factstmt.executeQuery(curSelect);
	    if (!rs.next()) { // if there is no row for the same PMID, then insert the row for the PMID
		if (abs != null && abs.length() > 0) {
		    newAbs = abs.replaceAll("\\\\", "\\\\\\\\");
		    newAbs = newAbs.replaceAll("\"", "\\\\\"");
		    String curInsertSQL = new String(
			    insertTitleAbstractString + PMID + "\",\"" + title + "\",\"" + abs + "\")");
		    factstmt.executeUpdate(curInsertSQL);
		} else {
		    String curInsertSQL = new String(insertTitleString + PMID + "\",\"" + title + "\")");
		    factstmt.executeUpdate(curInsertSQL);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void insertMetaData2DB(List<PubmedArticle> articles) {

	for (PubmedArticle art : articles) {
	    String PMID = art.getPMID();
	    String ISSN = art.getIssn();
	    String DP = art.getDatePublished();
	    String EDAT = art.getEDAT();
	    int pYear = art.getPYear();
	    String insertMetaSQL = new String(
		    insertMetaString + PMID + "\",\"" + ISSN + "\",\"" + DP + "\",\"" + EDAT + "\"," + pYear + ")");
	    // System.out.println(insertMetaSQL);
	    try {
		semmedstmt.executeUpdate(insertMetaSQL);
	    } catch (Exception e) {
		// e.printStackTrace();
	    }

	}
    }

    public static void main(String args[]) {
	StringBuilder sb = new StringBuilder();

	/*- try {
	    List<String> lines = FileUtils.linesFromFile("Z:\\Factuality\\TEES_processed\\11734154.xml", "UTF-8");
	    for (String line : lines)
		sb.append(line + "\n");
	    String text = sb.toString();
	    org.w3c.dom.Document doc = new Database().readFromXML(text);
	} catch (Exception e) {
	    e.printStackTrace();
	} */
	try {

	    /*- String[] tokens = new String[] { "To", "evaluate", "the", "economic", "impact", "in", "terms", "of", "direct",
	    "and", "indirect", "costs" };
	    
	    System.out.println("test");
	    String[] tags = OpennlpUtils.pos(tokens);
	    
	    String[] lemmas = OpennlpUtils.lemmatization(tokens, tags);
	    for (int i = 0; i < lemmas.length; i++) {
	    System.out.println(tokens[i] + " - " + lemmas[i] + " - " + tags[i]);
	    } */

	    SemRep2Database db = SemRep2Database.getInstance();
	    /*-  StringBuilder semBuilder = new StringBuilder();
	    // org.w3c.dom.Document doc = db.readDocumentFromDatabase("25574646", semBuilder);
	    nu.xom.Document doc = db.readDocumentFromDatabase("25574815", semBuilder);
	    long startTime = System.currentTimeMillis();
	    String semrep_data = semBuilder.toString();
	    XMLConverter xconverter = new XMLConverter();
	    String[] lines = semrep_data.split("\n");
	    doc = xconverter.changeStructureAndAddSemRepTerms(doc, lines);
	    
	    doc = xconverter.addLemmaToDocument(doc);
	    Serializer serializer = new Serializer(System.out, "ISO-8859-1");
	    // serializer.setIndent(4);
	    // serializer.setMaxLength(64);
	    serializer.write(doc);
	    // System.out.println(db.getStringFromDocument(doc));
	    long estimatedTime = System.currentTimeMillis() - startTime;
	    System.out.println(estimatedTime + " milisec"); */
	    List<String> semrepFiles = FileUtils.listFiles(args[0], false, "txt");
	    for (String inFileName : semrepFiles) {

		System.out.println(inFileName);
		db.saveSemRepToDatabase(inFileName);

	    }
	    /*- db.saveSemRepToDatabase("./Semrep_output/pubmed19n0084.txt");
	    
	    db.saveSemRepToDatabase("./Semrep_output/pubmed19n0093.txt");
	    db.saveSemRepToDatabase("./Semrep_output/pubmed19n0094.txt"); */
	} catch (Exception e) {
	    e.printStackTrace();
	}
	//   }

    }
}
