package gov.nih.nlm.skr.semrepProcess;

/** 
 * A collection of methods that are needed for processing MEDLINEBASELINE citations, parsing and semrepping
 * 
 * @author Dongwook Shin
 *
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

public class DocumentParsing {
    // static MedlineBaseline mb = MedlineBaseline.getInstance();
    static MedlineBaseline mb = MedlineBaseline.getInstance();
    static SchedulerBatchProcessing sbp = SchedulerBatchProcessing.getInstance();
    static Database db = Database.getInstance();
    static final String selectString = "select XML_DATA, SEMREP_DATA from FACT_DATA where PMID = \"";
    static final String insertXMLString = "insert into FACT_DATA (PMID, EXIST_XML, XML_DATA) VALUES (\"";
    static final String insertMetaString = "insert into CITATIONS (PMID, ISSN, DP, EDAT, PYEAR) VALUES (\"";

    private static DocumentParsing myInstance;
    static String dbName = null;
    static String dbUserName = null;
    static String dbPassword = null;

    static String dbloadingProgram = null;
    static Properties properties;

    static int successLen = 10000000;
    static int successParseLen = 1000000;

    public DocumentParsing() {
	try {
	    properties = FileUtils.loadPropertiesFromFile("semrep.properties");
	    dbName = properties.getProperty("database");
	    dbUserName = properties.getProperty("username");
	    dbPassword = properties.getProperty("password");
	    dbloadingProgram = properties.getProperty("dbLoadingProgram");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static DocumentParsing getInstance() {
	if (myInstance == null)
	    synchronized (Database.class) {
		if (myInstance == null)
		    myInstance = new DocumentParsing();
	    }
	return myInstance;
    }

    static public void processing2DB(String infileName, String ASCIIFileName, String normfileName,
	    String medlinefileName, String parsedfileName, String semrepfileName) {
	try {
	    // MedlineBaseline mb = MedlineBaseline.getInstance();
	    String schedulerAcc = properties.getProperty("schedulerAccount");
	    String schedulerPassword = properties.getProperty("schedulerPassword");
	    String email = properties.getProperty("schedulerEmail");
	    convert2ASCII(infileName, ASCIIFileName);
	    mb.convertXML2Text(ASCIIFileName, normfileName, db);
	    mb.convertXML2Medline(ASCIIFileName, medlinefileName);
	    sbp.submitTask(normfileName, parsedfileName, "sf_parser", schedulerAcc, schedulerPassword, email);
	    db.saveXmlToDatabase(parsedfileName);
	    //  db.saveSemRepToDatabase(semrepfileName);
	    // LoadingSemRep2DB insertDB = new LoadingSemRep2DB();
	    // insertDB.setPath("/usr/bin/perl", loadingProgram, semrepfileName, dbName, dbUserName, dbPassword); // if
	    // insertDB.start();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    static public void processingDir2DB(String inDir, String ASCIIDir, String normDir, String medlineDir,
	    String parsedDir, String semDir) {
	try {
	    // MedlineBaseline mb = MedlineBaseline.getInstance();

	    List<String> listInFiles = FileUtils.listFiles(inDir, false, "xml");
	    List<String> listASCIIFiles = FileUtils.listFiles(ASCIIDir, false, "xml");
	    List<String> parsedFiles = FileUtils.listFiles(parsedDir, false, "xml");
	    for (String inFileName : listInFiles) {
		int pos1 = inFileName.lastIndexOf(File.separator) + 1;
		String fileName = inFileName.substring(pos1).replace(".xml", "");
		String ASCIIFileName = new String(ASCIIDir + File.separator + fileName + ".xml");
		String XMLFileName = new String(parsedDir + File.separator + fileName + ".xml");
		if (!listASCIIFiles.contains(ASCIIFileName)) { // If the infileName was not converted before
		    File XMLFile = new File(XMLFileName);
		    if ((!parsedFiles.contains(XMLFileName))
			    || (parsedFiles.contains(XMLFileName) && XMLFile.length() < successParseLen)) {
			String normFileName = new String(normDir + File.separator + fileName + ".txt");
			String medlineFileName = new String(medlineDir + File.separator + fileName + ".txt");
			String parsedFileName = new String(parsedDir + File.separator + fileName + ".xml");
			String semFileName = new String(semDir + File.separator + fileName + ".txt");
			System.out.println("Processing " + inFileName);
			processing2DB(inFileName, ASCIIFileName, normFileName, medlineFileName, parsedFileName,
				semFileName);
		    }

		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    static public void update2NormMedline(String ASCIIFileName, String normFileName, String medlineFileName,
	    PrintWriter pw) {
	try {
	    mb.updateXML2Abstract(ASCIIFileName, normFileName, db, pw);
	    mb.convertXML2Medline(ASCIIFileName, medlineFileName);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    static public void update2NormMedlineDir2DB(String ASCIIDir, String normDir, String medlineDir, String PMIDfile) {
	try {
	    // MedlineBaseline mb = MedlineBaseline.getInstance();
	    PrintWriter pw = new PrintWriter(PMIDfile);
	    List<String> listASCIIFiles = FileUtils.listFiles(ASCIIDir, false, "xml");
	    for (String inFileName : listASCIIFiles) {
		int pos1 = inFileName.lastIndexOf(File.separator) + 1;
		String fileName = inFileName.substring(pos1).replace(".xml", "");
		String ASCIIFileName = new String(ASCIIDir + File.separator + fileName + ".xml");
		String normFileName = new String(normDir + File.separator + fileName + ".txt");
		String medlineFileName = new String(medlineDir + File.separator + fileName + ".txt");
		System.out.println("Processing " + inFileName);
		update2NormMedline(ASCIIFileName, normFileName, medlineFileName, pw);
	    }
	    pw.close();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    static public void semreppingDir2DB(String orgXMLDir, String ASCIIXMLDir, String medlineDir, String semDir) {
	try {
	    // MedlineBaseline mb = MedlineBaseline.getInstance();

	    List<String> listInOrgFiles = FileUtils.listFiles(orgXMLDir, false, "xml");
	    List<String> listInXMLFiles = FileUtils.listFiles(ASCIIXMLDir, false, "xml");
	    List<String> listmedlineFiles = FileUtils.listFiles(medlineDir, false, "txt");
	    List<String> listsemrepFiles = FileUtils.listFiles(semDir, false, "txt");
	    HashSet<String> successSemFiles = new HashSet();
	    for (String semfileName : listsemrepFiles) {
		File semFile = new File(semfileName);
		if (semFile.length() > successLen) {
		    int pos1 = semfileName.lastIndexOf(File.separator) + 1;
		    String fileName = semfileName.substring(pos1).replace(".txt", "");
		    successSemFiles.add(fileName);
		}
	    }

	    for (String inFileName : listInOrgFiles) {
		int pos1 = inFileName.lastIndexOf(File.separator) + 1;
		String fileName = inFileName.substring(pos1).replace(".xml", "");
		String ASCIIXMLFileName = new String(ASCIIXMLDir + File.separator + fileName + ".xml");
		if (!successSemFiles.contains(fileName)) { // If the infileName was not converted before
		    String medlineFileName = new String(medlineDir + File.separator + fileName + ".txt");
		    String semFileName = new String(semDir + File.separator + fileName + ".txt");
		    convert2ASCII(inFileName, ASCIIXMLFileName);
		    mb.convertXML2Medline(ASCIIXMLFileName, medlineFileName, db);
		    File mf = new File(medlineFileName);
		    if (mf.exists()) { // do the following only if MEDLINE file exists
			System.out.println("Processing " + inFileName);
			File XMLFile = new File(ASCIIXMLFileName);
			List<PubmedArticle> articles = mb.extractCitationInfo(XMLFile);
			db.insertMetaData2DB(articles);
			db.insertTitleAbstract(articles);
			semrepping2DB(medlineFileName, semFileName);
		    }
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    /*
     * Add only citations that are not yet loaded into SemMedDB
     */

    static public void semreppingAdditionDir2DB(String orgXMLDir, String ASCIIXMLDir, String medlineDir,
	    String semDir) {
	try {
	    // MedlineBaseline mb = MedlineBaseline.getInstance();

	    List<String> listInOrgFiles = FileUtils.listFiles(orgXMLDir, false, "xml");
	    List<String> listInXMLFiles = FileUtils.listFiles(ASCIIXMLDir, false, "xml");
	    List<String> listmedlineFiles = FileUtils.listFiles(medlineDir, false, "txt");
	    List<String> listsemrepFiles = FileUtils.listFiles(semDir, false, "txt");
	    HashSet<String> successSemFiles = new HashSet();
	    for (String semfileName : listsemrepFiles) {
		File semFile = new File(semfileName);
		if (semFile.length() > successLen) {
		    int pos1 = semfileName.lastIndexOf(File.separator) + 1;
		    String fileName = semfileName.substring(pos1).replace(".txt", "");
		    successSemFiles.add(fileName);
		}
	    }

	    for (String inFileName : listInOrgFiles) {
		int pos1 = inFileName.lastIndexOf(File.separator) + 1;
		String fileName = inFileName.substring(pos1).replace(".xml", "");
		String ASCIIXMLFileName = new String(ASCIIXMLDir + File.separator + fileName + ".xml");
		if (!successSemFiles.contains(fileName)) { // If the infileName was not converted before
		    String medlineFileName = new String(medlineDir + File.separator + fileName + ".txt");
		    String semFileName = new String(semDir + File.separator + fileName + ".txt");
		    convert2ASCII(inFileName, ASCIIXMLFileName);
		    mb.convertXML2Medline_Addition(ASCIIXMLFileName, medlineFileName, db);
		    try {
			System.out.println("Processing " + inFileName);
			File XMLFile = new File(ASCIIXMLFileName);
			List<PubmedArticle> articles = mb.extractCitationInfo(XMLFile);
			db.insertMetaData2DB(articles);
			// db.insertTitleAbstract(articles);
			semrepping2DB(medlineFileName, semFileName);
		    } catch (Exception e) {
			e.printStackTrace();
		    }

		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    static public void insertMetaData2DB(String ASCIIXMLDir) {
	try {
	    // MedlineBaseline mb = MedlineBaseline.getInstance();
	    List<String> listInXMLFiles = FileUtils.listFiles(ASCIIXMLDir, false, "xml");

	    for (String inFileName : listInXMLFiles) {
		int pos1 = inFileName.lastIndexOf(File.separator) + 1;
		String fileName = inFileName.substring(pos1).replace(".xml", "");
		String XMLFileName = new String(ASCIIXMLDir + File.separator + fileName + ".xml");
		File XMLFile = new File(XMLFileName);
		List<PubmedArticle> articles = mb.extractCitationInfo(XMLFile);
		db.insertMetaData2DB(articles);
	    }

	} catch (Exception e) {
	    // e.printStackTrace();
	}

    }

    static public void norm2DB(String normfileName, String parsedfileName) {
	try {
	    db.saveXmlToDatabase(parsedfileName);
	    // insertDB.setPath("/usr/bin/perl", loadingProgram, semrepfileName, dbName, dbUserName, dbPassword); // if
	    // insertDB.start();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    static public void insertParsedResultToDB(String parsedfileName) {

    }

    static public void saveXML2Database(String parsedfileName) {
	try {
	    // MedlineBaseline mb = MedlineBaseline.getInstance();

	    db.saveXmlToDatabase(parsedfileName);

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    static public void semrepping2DB(String medlinefileName, String semrepfileName) {
	try {
	    // MedlineBaseline mb = MedlineBaseline.getInstance();
	    File iFile = new File(medlinefileName);
	    if (iFile.length() > 0) {
		String schedulerAcc = properties.getProperty("schedulerAccount");
		String schedulerPassword = properties.getProperty("schedulerPassword");
		String email = properties.getProperty("schedulerEmail");
		sbp.submitTask(medlinefileName, semrepfileName, "semrep -F", schedulerAcc, schedulerPassword, email);
		db.saveSemRepToDatabase(semrepfileName);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    static public void parsing2DB(String normfileName, String parsedfileName) {
	try {
	    // MedlineBaseline mb = MedlineBaseline.getInstance();
	    String schedulerAcc = properties.getProperty("schedulerAccount");
	    String schedulerPassword = properties.getProperty("schedulerPassword");
	    String email = properties.getProperty("schedulerEmail");

	    sbp.submitTask(normfileName, parsedfileName, "sf_parser", schedulerAcc, schedulerPassword, email);
	    db.saveXmlToDatabase(parsedfileName);

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    static public void parsingDir2DB(String normDir, String parsedDir) {
	try {
	    // MedlineBaseline mb = MedlineBaseline.getInstance();

	    List<String> listnormFiles = FileUtils.listFiles(normDir, false, "txt");
	    List<String> listparsedFiles = FileUtils.listFiles(parsedDir, false, "txt");
	    HashSet<String> successParsedFiles = new HashSet();
	    for (String parsedfileName : listparsedFiles) {
		File semFile = new File(parsedfileName);
		if (semFile.length() > successLen) {
		    int pos1 = parsedfileName.lastIndexOf(File.separator) + 1;
		    String fileName = parsedfileName.substring(pos1).replace(".txt", "");
		    successParsedFiles.add(fileName);
		}
	    }

	    for (String inFileName : listnormFiles) {
		if (!successParsedFiles.contains(inFileName)) { // If the infileName was not converted before
		    String normFileName = new String(normDir + File.separator + inFileName + ".txt");
		    String parsedFileName = new String(parsedDir + File.separator + inFileName + ".txt");
		    System.out.println("Processing " + inFileName);

		    parsing2DB(normFileName, parsedFileName);

		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public static void convert2ASCII(String infilename, String outfilename) {

	try {
	    StringBuffer buf = new StringBuffer();
	    BufferedReader reader;
	    reader = new BufferedReader(new InputStreamReader(new FileInputStream(infilename), "UTF-8"));

	    String Line;
	    PrintWriter pr = new PrintWriter(new File(outfilename));
	    while ((Line = reader.readLine()) != null) {
		String result = replace_UTF8.ReplaceLooklike(Line);
		pr.println(result);
		/*-if (Line.length() > 0) {
		    int pos = Line.indexOf('<');
		    String Line2 = "";
		    if (pos > -1)
			Line2 = Line.substring(pos);
		    else
			Line2 = Line;
		
		   // System.out.println(ReplaceLooklike(Line2)); */

	    } // fi
	    pr.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void extractMetaInfo(String XMLFileName, String outfileName) {
	File XMLFile = new File(XMLFileName);
	try {
	    List<PubmedArticle> articles = mb.extractCitationInfo(XMLFile);
	    writeMetaData(articles, outfileName);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    static public void writeMetaData(List<PubmedArticle> articles) {
	try {
	    for (PubmedArticle art : articles) {
		String PMID = art.getPMID();
		String ISSN = art.getIssn();
		String DP = art.getDatePublished();
		String EDAT = art.getEDAT();
		int pYear = art.getPYear();
		db.insertMetaData2DB(articles);
		// System.out.println(PMID + "\t" + ISSN + "\t" + DP + "\t" + EDAT + "\t" + pYear);
	    }

	} catch (Exception e) {
	    // e.printStackTrace();
	}
    }

    static public void writeMetaData(List<PubmedArticle> articles, String outfile) {
	try {
	    PrintWriter pw = new PrintWriter(outfile);
	    for (PubmedArticle art : articles) {
		String PMID = art.getPMID();
		String ISSN = art.getIssn();
		String DP = art.getDatePublished();
		String EDAT = art.getEDAT();
		int pYear = art.getPYear();
		pw.println(PMID + "\t" + ISSN + "\t" + DP + "\t" + EDAT + "\t" + pYear);
		System.out.println(PMID + "\t" + ISSN + "\t" + DP + "\t" + EDAT + "\t" + pYear);
	    }
	    pw.close();
	} catch (Exception e) {
	    // e.printStackTrace();
	}
    }

    static public void main(String[] args) {
	/*- String infile = "C:\\Factuality_122018\\Medline_Baseline\\pubmed19n0800.xml";
	String normfile = "C:\\Factuality_122018\\Normalization\\pubmed19n0800.txt";
	String parseoutfile = "C:\\Factuality_122018\\SF_output\\pubmed19n0800.xml";
	String medlinefile = "C:\\Factuality_122018\\MEDLINE_output\\pubmed19n0800.txt";
	String semrepfile = "C:\\Factuality_122018\\Semrep_output\\pubmed19n0800.txt"; */

	/*- String infile = "C:\\Factuality_122018\\Medline_Baseline\\pubmed19n0900.xml";
	String normfile = "C:\\Factuality_122018\\Normalization\\pubmed19n0900.txt";
	String parseoutfile = "C:\\Factuality_122018\\SF_output\\pubmed19n0900.xml";
	String medlinefile = "C:\\Factuality_122018\\MEDLINE_output\\pubmed19n0900.txt";
	String semrepfile = "C:\\Factuality_122018\\Semrep_output\\pubmed19n0900.txt"; */

	/*- String infile = "C:\\Factuality_122018\\Medline_Baseline\\28304776.xml";
	String normfile = "C:\\Factuality_122018\\Normalization\\28304776.txt";
	String parseoutfile = "C:\\Factuality_122018\\SF_output\\28304776.xml";
	String medlinefile = "C:\\Factuality_122018\\MEDLINE_output\\28304776.txt";
	String semrepfile = "C:\\Factuality_122018\\Semrep_output\\28304776.txt"; */

	/*- String infile = "C:\\Factuality_122018\\Medline_Baseline\\pubmed19n0099.xml";
	String normfile = "C:\\Factuality_122018\\Normalization\\pubmed19n0099.txt";
	String outfile = "C:\\Factuality_122018\\SF_output\\pubmed19n0099.txt"; */

	/*
	 * String infile = "C:\\Factuality_122018\\MEDLINE_BASELINE\\BC_2010_10.xml";
	 * String normfile = "C:\\Factuality_122018\\Normalization\\BC_10.txt"; String
	 * parseoutfile = "C:\\Factuality_122018\\SF_output\\BC_10.xml"; String
	 * medlinefile = "C:\\Factuality_122018\\MEDLINE_output\\BC_10.txt"; &
	 */

	/*- String normfile = "C:\\Factuality_122018\\Normalization\\25039193.txt";
	String parseoutfile = "C:\\Factuality_122018\\SF_output\\25039193.xml"; */
	// processing2DB(infile, normfile, medlinefile, parseoutfile, semrepfile);
	// insertSemRep2DB(infile, medlinefile, semrepfile);
	// db.saveSemRepToDatabase(semrepfile);

	/*- String infile = "C:\\Factuality_122018\\MEDLINE_Baseline";
	String ASCIIfile = "C:\\Factuality_122018\\ASCII";
	String normfile = "C:\\Factuality_122018\\Normalization";
	String parseoutfile = "C:\\Factuality_122018\\SF_output";
	String medlinefile = "C:\\Factuality_122018\\MEDLINE_output";
	String semrepfile = "C:\\Factuality_122018\\Semrep_output"; */

	try {

	    DocumentParsing dp = DocumentParsing.getInstance();
	    //  String infile2 = dp.convert2ASCII(infile);
	    //  dp.processing2DB(infile2, normfile, medlinefile, parseoutfile, semrepfile);
	    // dp.processingDir2DB(infile, ASCIIfile, normfile, medlinefile, parseoutfile, semrepfile);
	    dp.processingDir2DB(args[0], args[1], args[2], args[3], args[4], args[5]);
	    // dp.extractMetaInfo("C:\\Factuality_122018\\ASCII\\pubmed19n0896.xml",
	    //	    "C:\\Factuality_122018\\ASCII\\pubmed19n0896.meta");
	    // dp.saveXML2Database("C:\\Factuality_122018\\SF_output\\pubmed19n0896.txt");
	    // dp.norm2DB(normfile, parseoutfile);
	    /*- mb.convertXML2Text(inFile, normFile, db); 
	    mb.convertXML2Medline(inFile, medlineFile); */
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
