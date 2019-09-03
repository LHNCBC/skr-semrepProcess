package gov.nih.nlm.skr.semrepProcess;

/** 
 * A collection of database methods that call either factuality or semmeddb
 * 
 * @author Dongwook Shin
 *
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import nu.xom.Element;

public class Database {
    static Properties properties;
    Connection factconn;
    Statement factstmt;
    Statement factstmt2;
    Connection semmedconn;
    Statement semmedstmt;
    static final String selectString = "select XML_DATA, SEMREP_DATA from FACT_DATA where PMID = \"";
    static final String insertTitleString = "insert into FACT_DATA (PMID, TITLE) VALUES (\"";

    static final String insertTitleAbstractString = "insert into FACT_DATA (PMID, TITLE, ABSTRACT) VALUES (\"";
    static final String updateAbstractString = "update FACT_DATA set ABSTRACT = \""; // $$$$$1\" where PMID =\"$$$$$2\"";
    static final String updateTitleAbstractString = "update FACT_DATA set TITLE = \""; // $$$$$1\" where PMID =\"$$$$$2\"";
    static final String resetEXISTString = "update FACT_DATA set EXIST_XML = 0, EXIST_SEMREP = 0 "; // $$$$$1\" where PMID =\"$$$$$2\"";
    static final String insertXMLString = "insert into FACT_DATA (PMID, EXIST_XML, XML_DATA) VALUES (\"";
    static final String updateXMLString = "update FACT_DATA set EXIST_XML = 1, XML_DATA = \""; // $$$$$1\" where PMID =\"$$$$$2\"";
    static final String insertSemRepString = "insert into FACT_DATA (PMID, EXIST_SEMREP, SEMREP_DATA) VALUES (\"";
    static final String updateSemRepString = "update FACT_DATA set EXIST_SEMREP = 1, SEMREP_DATA =  \""; // $$$$$1\" where PMID =\"$$$$$2\"";

    static final String insertMetaString = "insert into CITATIONS (PMID, ISSN, DP, EDAT, PYEAR) VALUES (\"";
    static final String selectCNoSemRepResultString = "select count(*) from FACT_DATA where EXIST_SEMREP = 0 and length(TITLE) > 0 ";
    static final String selectTitleAbstractString = "select * from FACT_DATA where EXIST_SEMREP = 0 and length(TITLE) > 0  limit & offset #";
    static final String selectFinalTitleAbstractString = "select * from FACT_DATA where EXIST_SEMREP = 0 and length(TITLE) > 0";
    static final String selectTitleAbstractPMIDString = "select * from FACT_DATA where PMID = \"";
    static final String selectTitleAbstractStringForParsing = "select * from FACT_DATA where EXIST_XML = 0  limit & offset #";
    static final String selectCNoParsingResultString = "select count(*) from FACT_DATA where EXIST_XML = 0 ";
    static final String selectSENTENCEString = "select count(*) from SENTENCE where PMID = \"";
    private static Database myInstance;

    static String connectionString;
    static String factdbName;
    static String semmeddbName;
    static String dbusername;
    static String dbpassword;
    static String perlScript;
    static String semrepLoadingProgram;
    static String entityLoadingProgram;
    static String normFile;

    public Database() {
	try {
	    properties = FileUtils.loadPropertiesFromFile("semrep.properties");
	    connectionString = properties.getProperty("connectionString");
	    factdbName = properties.getProperty("predatabase");
	    semmeddbName = properties.getProperty("semmeddatabase");
	    dbusername = properties.getProperty("dbusername");
	    dbpassword = properties.getProperty("dbpassword");
	    semrepLoadingProgram = properties.getProperty("semrepLoadingProgram");
	    entityLoadingProgram = properties.getProperty("semrepentityLoadingProgram");
	    perlScript = properties.getProperty("perlScript");
	    normFile = properties.getProperty("semrepNormFile");
	    Class.forName("com.mysql.jdbc.Driver");
	    factconn = DriverManager.getConnection(connectionString + "/" + factdbName + "?autoReconnect=true",
		    dbusername, dbpassword);
	    factstmt = factconn.createStatement();
	    factstmt2 = factconn.createStatement();
	    semmedconn = DriverManager.getConnection(connectionString + "/" + semmeddbName + "?autoReconnect=true",
		    dbusername, dbpassword);
	    semmedstmt = semmedconn.createStatement();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static Database getInstance() {
	if (myInstance == null)
	    synchronized (Database.class) {
		if (myInstance == null)
		    myInstance = new Database();
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
	    // e.printStackTrace();
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
	    // e.printStackTrace();
	}
    }

    public void loadSemRepToSemMedDB(String file) {
	try {
	    DatabaseBatch insertDB = new DatabaseBatch();
	    insertDB.setPath(perlScript, semrepLoadingProgram, file, semmeddbName, dbusername, dbpassword, normFile); // if
	    insertDB.start();
	} catch (Exception e) {
	    // e.printStackTrace();
	}
    }

    public void loadSemRepEntityToSemMedDB(String file) {
	try {
	    DatabaseBatch insertDB = new DatabaseBatch();
	    insertDB.setPath(perlScript, entityLoadingProgram, file, semmeddbName, dbusername, dbpassword, normFile); // if
	    insertDB.start();
	} catch (Exception e) {
	    // e.printStackTrace();
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

    /*
     * Insert Title and Abstract to PREPROCESS database
     */
    public void insertTitleAbstract(List<PubmedArticle> articles) {

	for (PubmedArticle art : articles) {
	    insertTitleAbstract(art.getPMID(), art.getTitle(), art.getAbstract());
	}
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
			    insertTitleAbstractString + PMID + "\",\"" + newTitle + "\",\"" + newAbs + "\")");
		    factstmt.executeUpdate(curInsertSQL);
		} else {
		    String curInsertSQL = null;
		    try {
			curInsertSQL = new String(insertTitleString + PMID + "\",\"" + newTitle + "\")");
			factstmt.executeUpdate(curInsertSQL);
		    } catch (Exception e) {
			System.out.println(curInsertSQL);
		    }
		}
	    }
	} catch (Exception e) {
	    // e.printStackTrace();
	}

    }

    public void updateAbstract(String PMID, String abs) {
	try {
	    String newAbs = abs.replaceAll("\\\\", "\\\\\\\\");
	    newAbs = newAbs.replaceAll("\"", "\\\\\"");
	    String curUpdateSQL = new String(updateAbstractString + newAbs + "\" where PMID = \"" + PMID + "\"");
	    // System.out.println(curUpdateSQL);
	    factstmt.executeUpdate(curUpdateSQL);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void updateTitleAbstract(String PMID, String title, String abs) {
	try {
	    String newTitle = title.replaceAll("\\\\", "\\\\\\\\");
	    newTitle = newTitle.replaceAll("\"", "\\\\\"");
	    String curUpdateSQL = null;
	    if (abs != null && abs.length() > 0) {
		String newAbs = abs.replaceAll("\\\\", "\\\\\\\\");
		newAbs = newAbs.replaceAll("\"", "\\\\\"");
		curUpdateSQL = new String(updateTitleAbstractString + newTitle + "\", ABSTRACT =\"" + newAbs
			+ "\" where PMID = \"" + PMID + "\"");
	    } else {
		curUpdateSQL = new String(
			updateTitleAbstractString + newTitle + "\", ABSTRACT =\"\" where PMID = \"" + PMID + "\"");

	    }
	    // System.out.println(curUpdateSQL);
	    factstmt.executeUpdate(curUpdateSQL);
	} catch (

	Exception e) {
	    e.printStackTrace();
	}
    }

    public void resetEXISTInfo(String PMID) {
	try {

	    String curUpdateSQL = new String(resetEXISTString + " where PMID = \"" + PMID + "\"");
	    // System.out.println(curUpdateSQL);
	    factstmt.executeUpdate(curUpdateSQL);
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

    /*- org.w3c.dom.Document readFromXML(String xmlString) {
    org.w3c.dom.Document doc = null;
    Document newDocument = null;
    try {
    
        DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
        newInstance.setNamespaceAware(true);
        doc = newInstance.newDocumentBuilder()
    	    .parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
        Element rootElem = doc.getDocumentElement();
        NamedNodeMap attrs = rootElem.getAttributes();
        Node docTextNode = attrs.getNamedItem("text");
        Node idNode = attrs.getNamedItem("origId");
        String docText = docTextNode.getTextContent();
        String PMID = idNode.getTextContent();
        System.out.println(docText);
        newDocument = new Document(PMID, docText); // initialize the document with text
        NodeList nodeList = rootElem.getElementsByTagName("sentence");
    
        for (int i = 0; i < nodeList.getLength(); i++) {
    	Node sentNode = nodeList.item(i);
    	NamedNodeMap sentAttrs = sentNode.getAttributes();
    	Node sentOffsetNode = sentAttrs.getNamedItem("charOffset");
    	String sentOffset = sentOffsetNode.getTextContent();
    	System.out.println(sentOffset);
        }
    
    } catch (Exception e) {
        e.printStackTrace();
    }
    
    return doc;
    } */

    public String getStringFromDocument(nu.xom.Document doc) {
	Element elem = doc.getRootElement();

	return elem.toString();

	/*- DOMSource domSource = new DOMSource(doc);
	StringWriter writer = new StringWriter();
	StreamResult result = new StreamResult(writer);
	TransformerFactory tf = TransformerFactory.newInstance();
	Transformer transformer = tf.newTransformer();
	transformer.transform(domSource, result);
	return writer.toString(); */
    }

    public void semrepFromDBSource(int limit, int offset, String TmpDir) {
	// static final String selectTitleAbstractString = "select * from FACT_DATA where EXIST_SEMREP = 0 limit & offset #";
	try {
	    DocumentParsing dp = DocumentParsing.getInstance();
	    ResultSet rscount = factstmt.executeQuery(selectCNoSemRepResultString);
	    rscount.next();
	    int total = rscount.getInt(1);
	    // Offset is provided from command line
	    // int offset = 0;
	    int fileNum = 1;
	    System.out.println("Total # of unsemrepped citations = " + total);
	    while (offset < total) {
		String selectTmp = selectTitleAbstractString.replace("&", Integer.toString(limit)).replace("#",
			Integer.toString(offset));
		System.out.println(selectTmp);
		ResultSet rs = factstmt.executeQuery(selectTmp);

		String medlineFileName = new String(TmpDir + File.separator + fileNum + ".txt");
		String semrepFileName = new String(TmpDir + File.separator + fileNum + ".semrep");
		PrintWriter pw = new PrintWriter(medlineFileName);
		fileNum++;
		while (rs.next()) {
		    StringBuffer sb = new StringBuffer();
		    String PMID = rs.getString("PMID");
		    String title = rs.getString("TITLE");
		    String abstractStr = rs.getString("ABSTRACT");
		    if (title != null && title.length() > 0) {
			sb.append("PMID- " + PMID + "\n");
			sb.append("TI  - " + title + "\n");
			if (abstractStr != null) {
			    sb.append("AB  - " + abstractStr + "\n\n");
			} else
			    sb.append("\n");
			// sb.append("DA  - ABC\n\n"); // add arbitrary string in order to avoid semrep to crash
		    }
		    pw.println(sb.toString());
		    pw.flush();
		}
		pw.close();
		System.out.println("Text written from database to file completed : " + medlineFileName);
		offset = offset + limit;
		String schedulerAcc = dp.properties.getProperty("schedulerAccount");
		String schedulerPassword = dp.properties.getProperty("schedulerPassword");
		dp.sbp.submitTask(medlineFileName, semrepFileName, "semrep -F", schedulerAcc, schedulerPassword,
			"shindongwoo@nih.gov");
		saveSemRepToDatabase(semrepFileName);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void extractUnsemreppedCitationsFromDBSource(String TmpDir) {
	// static final String selectTitleAbstractString = "select * from FACT_DATA where EXIST_SEMREP = 0 limit & offset #";
	try {
	    DocumentParsing dp = DocumentParsing.getInstance();
	    ResultSet rscount = factstmt.executeQuery(selectCNoSemRepResultString);
	    rscount.next();
	    int total = rscount.getInt(1);
	    System.out.println("Total # of unsemrepped citations = " + total);
	    ResultSet rs = factstmt.executeQuery(selectFinalTitleAbstractString);

	    String medlineFileName = new String(TmpDir + File.separator + "final.txt");
	    String pmidFileName = new String(TmpDir + File.separator + "final.PMID");
	    PrintWriter pw = new PrintWriter(medlineFileName);
	    PrintWriter pw2 = new PrintWriter(pmidFileName);
	    while (rs.next()) {
		StringBuffer sb = new StringBuffer();
		String PMID = rs.getString("PMID");
		String title = rs.getString("TITLE");
		String abstractStr = rs.getString("ABSTRACT");
		if (title != null && title.length() > 0) {
		    sb.append("PMID- " + PMID + "\n");
		    sb.append("TI  - " + title + "\n");
		    if (abstractStr != null && abstractStr.length() > 0) {
			sb.append("AB  - " + abstractStr + "\n");
			sb.append("DA  - ABC\n\n"); // add arbitrary string in order to avoid semrep to crash
		    } else
			// sb.append("\n");
			sb.append("DA  - ABC\n\n"); // add arbitrary string in order to avoid semrep to crash
		}
		pw.println(sb.toString());
		pw2.println(PMID);
		pw.flush();
		pw2.flush();
	    }
	    pw.close();
	    System.out.println("Text written from database to file completed : " + medlineFileName);

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void extractUnsemreppedCitationsFromDBSourcePMIDFile(String TmpDir, String pmidfile) {
	// static final String selectTitleAbstractString = "select * from FACT_DATA where EXIST_SEMREP = 0 limit & offset #";
	try {
	    DocumentParsing dp = DocumentParsing.getInstance();
	    List<String> pmidlist = FileUtils.linesFromFile(pmidfile, "UTF-8");

	    ResultSet rs = factstmt.executeQuery(selectTitleAbstractPMIDString);
	    String medlineFileName = new String(TmpDir + File.separator + "final.txt");
	    String pmidFileName = new String(TmpDir + File.separator + "final.PMID");
	    PrintWriter pw = new PrintWriter(medlineFileName);
	    PrintWriter pw2 = new PrintWriter(pmidFileName);
	    for (String pmid : pmidlist) {

		while (rs.next()) {
		    StringBuffer sb = new StringBuffer();
		    String PMID = rs.getString("PMID");
		    String title = rs.getString("TITLE");
		    String abstractStr = rs.getString("ABSTRACT");
		    if (title != null && title.length() > 0) {
			sb.append("PMID- " + PMID + "\n");
			sb.append("TI  - " + title + "\n");
			if (abstractStr != null && abstractStr.length() > 0) {
			    sb.append("AB  - " + abstractStr + "\n");
			    sb.append("DA  - ABC\n\n"); // add arbitrary string in order to avoid semrep to crash
			} else
			    // sb.append("\n");
			    sb.append("DA  - ABC\n\n"); // add arbitrary string in order to avoid semrep to crash
		    }
		    pw.println(sb.toString());
		    pw2.println(PMID);
		    pw.flush();
		    pw2.flush();
		}
	    }
	    pw.close();
	    System.out.println("Text written from database to file completed : " + medlineFileName);

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void parsingFromDBSource(int limit, int start, String TmpDir) {
	// static final String selectTitleAbstractString = "select * from FACT_DATA where EXIST_SEMREP = 0 limit & offset #";
	try {
	    DocumentParsing dp = DocumentParsing.getInstance();
	    ResultSet rscount = factstmt.executeQuery(selectCNoParsingResultString);
	    rscount.next();
	    int total = rscount.getInt(1);
	    int offset = start;
	    int fileNum = 1;
	    System.out.println("Total # of un-parsed citations = " + total);
	    while (offset < total) {
		String selectTmp = selectTitleAbstractStringForParsing.replace("&", Integer.toString(limit))
			.replace("#", Integer.toString(offset));
		System.out.println(selectTmp);
		ResultSet rs = factstmt.executeQuery(selectTmp);

		String medlineFileName = new String(TmpDir + File.separator + fileNum + ".txt");
		String xmlFileName = new String(TmpDir + File.separator + fileNum + ".xml");
		PrintWriter pw = new PrintWriter(medlineFileName);
		fileNum++;
		while (rs.next()) {
		    StringBuffer sb = new StringBuffer();
		    String PMID = rs.getString("PMID");
		    String title = rs.getString("TITLE");
		    String abstractStr = rs.getString("ABSTRACT");
		    if (title != null && title.length() > 0) {
			sb.append(PMID + "\n");
			sb.append(title + "\n");
			if (abstractStr != null) {
			    sb.append(abstractStr + "\n\n");
			} else
			    sb.append("\n");
		    }
		    pw.println(sb.toString());
		    pw.flush();
		}
		pw.close();
		System.out.println("Text written from database to file completed : " + medlineFileName);
		offset = offset + limit;
		String schedulerAcc = dp.properties.getProperty("schedulerAccount");
		String schedulerPassword = dp.properties.getProperty("schedulerPassword");
		dp.sbp.submitTask(medlineFileName, xmlFileName, "sf_parser", schedulerAcc, schedulerPassword,
			"shindongwoo@nih.gov");
		saveXmlToDatabase(xmlFileName);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void parsingFromDBSourceToFile(int limit, int start, String fileName) {
	// static final String selectTitleAbstractString = "select * from FACT_DATA where EXIST_SEMREP = 0 limit & offset #";
	try {
	    DocumentParsing dp = DocumentParsing.getInstance();
	    ResultSet rscount = factstmt.executeQuery(selectCNoParsingResultString);
	    rscount.next();
	    int total = rscount.getInt(1);
	    int offset = start;
	    System.out.println("Total # of un-parsed citations = " + total);
	    String selectTmp = selectTitleAbstractStringForParsing.replace("&", Integer.toString(limit)).replace("#",
		    Integer.toString(offset));
	    System.out.println(selectTmp);
	    ResultSet rs = factstmt.executeQuery(selectTmp);

	    PrintWriter pw = new PrintWriter(fileName);
	    while (rs.next()) {
		StringBuffer sb = new StringBuffer();
		String PMID = rs.getString("PMID");
		String title = rs.getString("TITLE");
		String abstractStr = rs.getString("ABSTRACT");
		if (title != null && title.length() > 0) {
		    sb.append(PMID + "\n");
		    sb.append(title + "\n");
		    if (abstractStr != null) {
			sb.append(abstractStr + "\nEOPF\n");
		    } else
			sb.append("EOPF\n");
		}
		pw.println(sb.toString());
		pw.flush();
	    }
	    pw.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /*
     * Database utils
     */
    public List FindPMIDInFACTDATANotInCITATION(String filename) {
	List<String> PMIDList = new ArrayList<>();
	List<String> PMIDListFD = new ArrayList<>();
	Set<String> PMIDSetSM = new HashSet<>();
	String selectFACTDATA1 = new String("select PMID from FACT_DATA order by PMID");
	String selectSEMMED1 = new String("select PMID from CITATIONS order by PMID");

	try {
	    PrintWriter pw = new PrintWriter(filename);
	    ResultSet rsFACTDATA1 = factstmt.executeQuery(selectFACTDATA1);
	    while (rsFACTDATA1.next()) {
		PMIDListFD.add(rsFACTDATA1.getString("PMID"));
	    }

	    ResultSet rsSEMMED1 = semmedstmt.executeQuery(selectSEMMED1);
	    while (rsSEMMED1.next()) {
		PMIDSetSM.add(rsSEMMED1.getString("PMID"));
	    }

	    for (String PMID1 : PMIDListFD) {
		if (!PMIDSetSM.contains(PMID1)) {
		    pw.println(PMID1);
		    pw.flush();
		    PMIDList.add(PMID1);
		}

	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return PMIDList;
    }

    public void FindPMIDInCITATIONNotInSENTENCE(String filename1, String filename2) {
	List<String> PMIDList = new ArrayList<>();
	List<String> PMIDCITATIONList = new ArrayList<>();
	Set<String> PMIDSENTENCESet = new HashSet<>();
	String getSEMREPDATA = new String("select SEMREP_DATA from FACT_DATA where PMID=\"");
	String selectCITATION = new String("select PMID from CITATIONS order by PMID");
	String selectSENTENCE = new String("select DISTINCT PMID from SENTENCE");

	try {
	    PrintWriter pw1 = new PrintWriter(filename1);
	    PrintWriter pw2 = new PrintWriter(filename2);
	    ResultSet rsCITATION = semmedstmt.executeQuery(selectCITATION);
	    System.out.println("retrieving PMIDs from CITATIONS");
	    while (rsCITATION.next()) {
		PMIDCITATIONList.add(rsCITATION.getString("PMID"));
	    }
	    rsCITATION.close();

	    System.out.println("retrieving PMIDs from SENTENCE");
	    ResultSet rsSENTENCE = semmedstmt.executeQuery(selectSENTENCE);
	    while (rsSENTENCE.next()) {
		PMIDSENTENCESet.add(rsSENTENCE.getString("PMID"));
	    }
	    rsSENTENCE.close();

	    System.out.println("retrieving PMIDs not in SENTENCE");
	    for (String PMID1 : PMIDCITATIONList) {
		if (!PMIDSENTENCESet.contains(PMID1)) {
		    String selectData = new String(getSEMREPDATA + PMID1 + "\"");
		    ResultSet rsFACT = factstmt.executeQuery(selectData);
		    try {
			rsFACT.next();
			String SemRepDATA = rsFACT.getString(1);
			pw1.println(SemRepDATA + "\n");
			pw1.flush();
			pw2.println(PMID1);
			rsFACT.close();
		    } catch (Exception e) {
			System.out.println(PMID1);
			// e.printStackTrace();
		    }
		}

	    }
	    pw1.close();
	    pw2.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void populateNormalizedSH2DB(String normalizeSHTxt, int startOffset, int limit) {
	try {
	    Map<String, String> SHMap = new HashMap<>();
	    BufferedReader SECTIONHEADERin = new BufferedReader(new FileReader(normalizeSHTxt));
	    Statement stmt1 = semmedconn.createStatement();
	    Statement stmt2 = semmedconn.createStatement();
	    // String SQLselect = new String("select DISTINCT(c.CUI), c.PREFERRED_NAME from CONCEPT as c, CONCEPT_SEMTYPE as cs where c.CONCEPT_ID = cs.CONCEPT_ID and cs.NOVEL = 'N'");
	    String SQLselectTotal = new String("select count(*) from SENTENCE");
	    String SQLselectTmp = new String("select SENTENCE_ID, SECTION_HEADER from SENTENCE  limit 222 offset 111");
	    String SQLupdateOrg = new String(
		    "update SENTENCE set NORMALIZED_SECTION_HEADER = \"111\" where SENTENCE_ID = ");

	    String line = null;
	    int count = 0;
	    // Create a map for Section header -> 
	    while ((line = SECTIONHEADERin.readLine()) != null) {
		String[] compo = line.split("\\|");
		SHMap.put(compo[0], compo[1]);
		// System.out.println(compo[0] + " -> " + compo[1]);
	    }

	    ResultSet rs = stmt1.executeQuery(SQLselectTotal);
	    int total = 0;
	    while (rs.next()) {
		total = rs.getInt(1);
	    }
	    System.out.println("Total number of sentence = " + total);

	    int offset = startOffset;
	    while (offset < total) {
		String SQLselectCurrent = SQLselectTmp.replace("111", Integer.toString(offset)).replace("222",
			Integer.toString(limit));
		// System.out.println(SQLselectCurrent);
		ResultSet rs1 = stmt1.executeQuery(SQLselectCurrent);
		while (rs1.next()) {
		    int sentenceid = rs1.getInt(1);
		    String SH = rs1.getString(2);
		    String NSH = null;
		    NSH = SHMap.get(SH);
		    if (NSH == null)
			NSH = new String("");
		    String SQLupdate = SQLupdateOrg.replace("111", NSH);
		    SQLupdate = new String(SQLupdate + sentenceid);
		    // System.out.println(SQLupdate);
		    stmt2.executeUpdate(SQLupdate);
		}
		offset = offset + limit;
	    }

	} catch (Exception e) {
	    // e.printStackTrace();
	}
	System.out.println("total sentence processed");

    }

    public void populateNormalizedSH2DBFile(String normalizeSHTxt, String pmidFile) {
	try {
	    Map<String, String> SHMap = new HashMap<>();
	    BufferedReader SECTIONHEADERin = new BufferedReader(new FileReader(normalizeSHTxt));
	    BufferedReader pmidBR = new BufferedReader(new FileReader(pmidFile));
	    Statement stmt1 = semmedconn.createStatement();
	    Statement stmt2 = semmedconn.createStatement();
	    // String SQLselect = new String("select DISTINCT(c.CUI), c.PREFERRED_NAME from CONCEPT as c, CONCEPT_SEMTYPE as cs where c.CONCEPT_ID = cs.CONCEPT_ID and cs.NOVEL = 'N'");
	    String SQLselectTmp = new String("select SENTENCE_ID, SECTION_HEADER from SENTENCE  where PMID = \"111\"");
	    String SQLupdateOrg = new String(
		    "update SENTENCE set NORMALIZED_SECTION_HEADER = \"111\" where SENTENCE_ID = ");
	    // ResultSet rs1 = stmt1.executeQuery(SQLselect);
	    System.out.println("Find all the Predications");
	    String line = null;
	    int count = 0;
	    // Create a map for Section header -> 
	    while ((line = SECTIONHEADERin.readLine()) != null) {
		String[] compo = line.split("\\|");
		SHMap.put(compo[0], compo[1]);
		System.out.println(compo[0] + " -> " + compo[1]);
	    }

	    int total = 0;
	    while ((line = pmidBR.readLine()) != null) {
		String SQLselectCurrent = SQLselectTmp.replace("111", line.trim());
		// System.out.println(SQLselectCurrent);
		ResultSet rs1 = stmt1.executeQuery(SQLselectCurrent);
		while (rs1.next()) {
		    int sentenceid = rs1.getInt(1);
		    String SH = rs1.getString(2);
		    String NSH = null;
		    NSH = SHMap.get(SH);
		    if (NSH == null)
			NSH = new String("");
		    String SQLupdate = SQLupdateOrg.replace("111", NSH);
		    SQLupdate = new String(SQLupdate + sentenceid);
		    // System.out.println(SQLupdate);
		    stmt2.executeUpdate(SQLupdate);

		}
		total++;
		System.out.println(total);
		rs1.close();

	    }

	} catch (Exception e) {
	    System.out.println("total sentence processed");
	    e.printStackTrace();
	}

    }

    public void populateNoveltyToPredication(int startOffset, int limit) {

	try {
	    Statement stmt1 = semmedconn.createStatement();
	    Statement stmt2 = semmedconn.createStatement();
	    // String SQLselect = new String("select DISTINCT(c.CUI),
	    // c.PREFERRED_NAME from CONCEPT as c, CONCEPT_SEMTYPE as cs where
	    // c.CONCEPT_ID = cs.CONCEPT_ID and cs.NOVEL = 'N'");
	    String SQLselect = new String(
		    "select PREDICATION_ID, SUBJECT_CUI, OBJECT_CUI from PREDICATION   limit 111 offset 222");
	    String SQLselect2Tmp = new String("select count(*) from GENERIC_CONCEPT where CUI = '");
	    String SQLupdateOrg = new String(
		    "update PREDICATION set SUBJECT_NOVELTY = 1, OBJECT_NOVELTY = 2 where PREDICATION_ID = ");

	    String SQLselectTotal = new String("select count(*) from PREDICATION");

	    String line = null;
	    int count = 0;
	    ResultSet rs = stmt1.executeQuery(SQLselectTotal);
	    int total = 0;
	    while (rs.next()) {
		total = rs.getInt(1);
	    }
	    System.out.println("Total number of predications = " + total);
	    int offset = startOffset;
	    while (offset < total) {
		String SQLselectTmp = SQLselect.replace("111", Integer.toString(limit)).replace("222",
			Integer.toString(offset));
		// System.out.println(SQLselectTmp);
		ResultSet rs1 = stmt1.executeQuery(SQLselectTmp);
		while (rs1.next()) {
		    int predicationid = rs1.getInt(1);
		    String scui = rs1.getString(2);
		    String ocui = rs1.getString(3);

		    ResultSet rs2 = stmt2.executeQuery(new String(SQLselect2Tmp + scui + "'"));
		    rs2.first();
		    int count1 = rs2.getInt(1);

		    ResultSet rs3 = stmt2.executeQuery(new String(SQLselect2Tmp + ocui + "'"));
		    rs3.first();
		    String SQLupdate = null;
		    int count2 = rs3.getInt(1);
		    if (count1 > 0 && count2 > 0) {
			SQLupdate = SQLupdateOrg.replaceFirst("1", "0").replaceFirst("2", "0");
		    } else if (count1 == 0 && count2 > 0) {
			SQLupdate = SQLupdateOrg.replaceFirst("1", "1").replaceFirst("2", "0");
		    } else if (count1 > 0 && count2 == 0) {
			SQLupdate = SQLupdateOrg.replaceFirst("1", "0").replaceFirst("2", "1");
		    } else if (count1 == 0 && count2 == 0) {
			SQLupdate = SQLupdateOrg.replaceFirst("1", "1").replaceFirst("2", "1");
		    }
		    SQLupdate = new String(SQLupdate + predicationid);
		    // System.out.println(SQLupdate);
		    stmt2.executeUpdate(SQLupdate);
		    count++;
		    // System.out.println(count);
		}
		offset = offset + limit;
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void removePMIDFromSentenceTable(String PMID) {
	try {
	    String SQLdelete = new String("delete from SENTENCE where PMID =\"111\"");
	    semmedstmt.executeUpdate(SQLdelete.replace("111", PMID));
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    static String selectSQL = "select TITLE, ABSTRACT From FACT_DATA where PMID = \"";
    static String updateSQL = "update FACT_DATA set TITLE = \"";

    public void updateToASCIIFromDBTitleAbstract(String PMID) throws Exception {
	try {
	    String curSelectSQL = selectSQL + PMID + "\"";
	    ResultSet rs2 = factstmt2.executeQuery(curSelectSQL);
	    rs2.next();
	    String orgTitle = rs2.getString("TITLE");
	    String orgAbstract = rs2.getString("ABSTRACT");
	    String Title = null;
	    String Abstract = null;
	    if (orgTitle != null) {
		Title = replace_UTF8.ReplaceLooklike(orgTitle);
		Title = Title.replaceAll("\\\\", "\\\\\\\\");
		Title = Title.replaceAll("\"", "\\\\\"");
	    }
	    if (orgAbstract != null) {
		Abstract = replace_UTF8.ReplaceLooklike(orgAbstract);
		Abstract = Abstract.replaceAll("\\\\", "\\\\\\\\");
		Abstract = Abstract.replaceAll("\"", "\\\\\"");
	    }

	    String curUpdateSQL = new String(
		    updateSQL + Title + "\", ABSTRACT = \"" + Abstract + "\" where PMID = \"" + PMID + "\"");
	    // System.out.println(curUpdateSQL);
	    rs2.close();
	    factstmt2.executeUpdate(curUpdateSQL);
	} catch (Exception e) {
	    System.out.println(PMID);
	    e.printStackTrace();
	}
    }

    static String selectSemRepZeroSQL = "select PMID From FACT_DATA where EXIST_SEMREP = 0";

    public void updateToASCIITitleAbstractFromSemRepZero() {
	try {
	    ResultSet rs1 = factstmt.executeQuery(selectSemRepZeroSQL);
	    System.out.println("Retrieve all PMIDs");
	    int count = 0;
	    while (rs1.next()) {
		String PMID = rs1.getString("PMID");
		count++;

		updateToASCIIFromDBTitleAbstract(PMID);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /*
     * Check if the PMID is already in the database. If true, return true, else
     * return false
     */
    public boolean checkPMIDInDB(String PMID) {
	int count = 0;
	try {
	    ResultSet rs = semmedstmt.executeQuery(selectSENTENCEString + PMID + "\"");
	    while (rs.next()) {
		count = rs.getInt(1);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	if (count == 0)
	    return false;
	else
	    return true;
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

	    Database db = Database.getInstance();
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
	    db.saveSemRepToDatabase(args[0]);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	//   }

    }
}
