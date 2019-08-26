package gov.nih.nlm.skr.semrepProcess;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

public class DBUtils {
    static Connection conn;
    static Statement stmt;
    static final String selectString = "select PUBMED_DATA, SEMREP_DATA from FACT_DATA where PMID = \"";
    static final String insertXMLString = "insert into FACT_DATA (PMID, EXIST_XML, EXIST_SEMREP, XML_DATA) values (\"";
    static final String updateSemRepString = "update FACT_DATA set EXIST_SEMREP = 1, SEMREP_DATA = \"";

    public DBUtils() {

    }

    public static void init(String dbName, String DBusername, String DBpassword) {
	try {
	    Class.forName("com.mysql.jdbc.Driver");
	    conn = DriverManager.getConnection("jdbc:mysql://indsrv2.nlm.nih.gov/" + dbName + "?autoReconnect=true",
		    DBusername, DBpassword);
	    stmt = conn.createStatement();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void importXMLToDB(String XMLDir) {
	try {
	    List<String> xmlfiles = FileUtils.listFiles(XMLDir, false, "xml");

	    for (String xmlfile : xmlfiles) {
		StringBuilder sb = new StringBuilder();
		List<String> xmllines = FileUtils.linesFromFile(xmlfile, "UTF-8");
		String PMID = xmlfile.substring(xmlfile.lastIndexOf(File.separator) + 1).replace(".xml", "");
		// String PMID = xmlfile.substring(0, xmlfile.indexOf("."));
		System.out.println(PMID);
		;
		for (String xmlline : xmllines) {
		    sb.append(xmlline + "\n");
		}
		String xmlstr = sb.toString();
		xmlstr = xmlstr.replaceAll("\\\\", "\\\\\\\\");
		xmlstr = xmlstr.replaceAll("\"", "\\\\\"");

		String curXMLString = new String(insertXMLString + PMID + "\", 1, 0, \"" + xmlstr + "\")");
		// System.out.println(curXMLString);
		stmt.executeUpdate(curXMLString);
	    }
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void importSemRepToDB(String SemRepDir) {
	try {
	    List<String> semrepfiles = FileUtils.listFiles(SemRepDir, false, "txt");

	    for (String semrepfile : semrepfiles) {
		StringBuilder sb = new StringBuilder();
		List<String> xmllines = FileUtils.linesFromFile(semrepfile, "UTF-8");
		String PMID = semrepfile.substring(semrepfile.lastIndexOf(File.separator) + 1).replace(".txt", "");
		// String PMID = xmlfile.substring(0, xmlfile.indexOf("."));
		System.out.println(PMID);
		;
		for (String xmlline : xmllines) {
		    sb.append(xmlline + "\n");
		}
		String semrepstr = sb.toString();
		semrepstr = semrepstr.replaceAll("\\\\", "\\\\\\\\");
		semrepstr = semrepstr.replaceAll("\"", "\\\\\"");

		String curSemRepString = new String(
			updateSemRepString + semrepstr + "\"" + "where PMID = \"" + PMID + "\"");
		// System.out.println(curXMLString);
		stmt.executeUpdate(curSemRepString);
	    }
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    static public void main(String argv[]) {
	init("FACTUALITY", "root", "indsrv2@root");
	// importXMLToDB("C:\\Factuality_data\\xmlDir");
	importSemRepToDB("C:\\Factuality_data\\semrep1.8Dir");
    }
}
