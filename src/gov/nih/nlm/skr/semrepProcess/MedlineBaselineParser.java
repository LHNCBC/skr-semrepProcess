package gov.nih.nlm.skr.semrepProcess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * Sax parser for a list of articles of pubmed, as returned by the EFetch web
 * service (in xml format).
 *
 *
 * @author Dongwook Shin
 *
 */
public class MedlineBaselineParser extends DefaultHandler {

    private PubmedArticle currentArticle;
    private boolean inID = false;
    private boolean inTitle = false;
    private boolean inAbstract = false;
    private boolean inPubDate = false;
    private boolean inDPMonth = false;
    private boolean inDPYear = false;
    private boolean inDPDay = false;
    private boolean inMedlineDate = false;

    private boolean inDateCreated = false;
    private boolean inDAMonth = false;
    private boolean inDAYear = false;
    private boolean inDADay = false;

    private boolean inDateCompleted = false;
    private boolean inDCOMMonth = false;
    private boolean inDCOMYear = false;
    private boolean inDCOMDay = false;

    private boolean inIssn = false;

    private StringBuffer sbAbstract;
    private StringBuffer sbTitle;
    private StringBuffer sbAuthor;
    private StringBuffer sbID;
    private StringBuffer sbMetadata;
    private StringBuffer sbIssn;

    private String DAyear;
    private String DAmonth;
    private String DAday;
    private String DADate;
    private String DCOMyear;
    private String DCOMmonth;
    private String DCOMday;
    private String DCOMDate;
    private String DPyear;
    private String DPmonth;
    private String DPday;
    private String DPDate;
    private String MedlineDate = null;

    private List<PubmedArticle> articles = new ArrayList<>();
    private boolean inMetaData = false;
    private boolean inPubType = false;
    private boolean inMeshHeading = false;
    private HashSet<String> metadataList = null;

    public List<PubmedArticle> getArticles() {
	return articles;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
	String eName = localName; // element name
	if ("".equals(eName))
	    eName = qName; // namespaceAware = false

	if (eName.equalsIgnoreCase("medlinecitation")) {
	    currentArticle = new PubmedArticle();
	    sbAbstract = new StringBuffer();
	    sbID = null;
	    inTitle = false;
	    inAbstract = false;
	} else if (eName.equalsIgnoreCase("pmid") && sbID == null) {
	    inID = true;
	    sbID = new StringBuffer();
	} else if (eName.equalsIgnoreCase("articletitle")) {
	    inTitle = true;
	    inAbstract = false;
	    sbTitle = new StringBuffer();
	} else if (eName.equalsIgnoreCase("abstracttext")) {
	    inTitle = false;
	    inAbstract = true;
	    // sbAbstract = new StringBuffer();
	} else if (eName.equalsIgnoreCase("datecreated"))
	    inDateCreated = true;
	else if (inDateCreated && eName.equalsIgnoreCase("year"))
	    inDAYear = true;
	else if (inDateCreated && eName.equalsIgnoreCase("month"))
	    inDAMonth = true;
	else if (inDateCreated && eName.equalsIgnoreCase("day"))
	    inDADay = true;
	else if (eName.equalsIgnoreCase("datecompleted"))
	    inDateCompleted = true;
	else if (inDateCompleted && eName.equalsIgnoreCase("year"))
	    inDCOMYear = true;
	else if (inDateCompleted && eName.equalsIgnoreCase("month"))
	    inDCOMMonth = true;
	else if (inDateCompleted && eName.equalsIgnoreCase("day"))
	    inDCOMDay = true;
	else if (eName.equalsIgnoreCase("pubdate"))
	    inPubDate = true;
	else if (inPubDate && eName.equalsIgnoreCase("year"))
	    inDPYear = true;
	else if (inPubDate && eName.equalsIgnoreCase("month"))
	    inDPMonth = true;
	else if (inPubDate && eName.equalsIgnoreCase("day"))
	    inDPDay = true;
	else if (inPubDate && eName.equalsIgnoreCase("medlinedate"))
	    inMedlineDate = true;
	else if (eName.equalsIgnoreCase("issn")) {
	    inIssn = true;
	    sbIssn = new StringBuffer();
	    // System.out.println("Start ISSN tag.");
	}
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
	String eName = localName; // element name
	if ("".equals(eName))
	    eName = qName; // namespaceAware = false

	if (eName.equalsIgnoreCase("medlinecitation")) {
	    currentArticle.setAbstract(sbAbstract.toString());
	    articles.add(currentArticle);
	} else if (eName.equalsIgnoreCase("PMID")) {
	    inID = false;
	    currentArticle.setPMID(sbID.toString().trim());
	    // System.out.println("PMID = " + currentArticle.getPMID());

	} else if (eName.equalsIgnoreCase("articletitle")) {
	    currentArticle.setTitle(sbTitle.toString());
	} else if (eName.equalsIgnoreCase("abstracttext")) {
	    // currentArticle.setAbstract(sbAbstract.toString());
	    sbAbstract.append(" "); // To prepare another AbstractText to be appended after this
	} else if (eName.equalsIgnoreCase("DateCreated")) {
	    inDateCreated = inDADay = inDAMonth = inDAYear = false;
	    String DADate = "";
	    if (DAyear != null) {
		DADate = DAyear;
		DAyear = null;
		if (DAmonth != null) {
		    DADate += " " + DAmonth;
		    DAmonth = null;
		    if (DAday != null) {
			DADate += " " + DAday;
			DAday = null;
		    }
		}
	    }
	    currentArticle.setDateCreated(DADate);
	} else if (inDateCreated && eName.equalsIgnoreCase("Year"))
	    inDAYear = false;
	else if (inDateCreated && eName.equalsIgnoreCase("Month"))
	    inDAMonth = false;
	else if (inDateCreated && eName.equalsIgnoreCase("Day"))
	    inDADay = false;
	else if (eName.equalsIgnoreCase("DateCompleted")) {
	    inDateCompleted = inDCOMDay = inDCOMMonth = inDCOMYear = false;
	    String DCOMDate = "";
	    if (DCOMyear != null) {
		DCOMDate = DCOMyear;
		DCOMyear = null;
		if (DCOMmonth != null) {
		    DCOMDate += " " + DCOMmonth;
		    DCOMmonth = null;
		    if (DCOMday != null) {
			DCOMDate += " " + DCOMday;
			DCOMday = null;
		    }
		}
	    }
	    // currentArticle.setDateCompleted(DCOMDate);
	} else if (inDateCompleted && eName.equalsIgnoreCase("Year"))
	    inDCOMYear = false;
	else if (inDateCompleted && eName.equalsIgnoreCase("Month"))
	    inDCOMMonth = false;
	else if (inDateCreated && eName.equalsIgnoreCase("Day"))
	    inDCOMDay = false;
	else if (eName.equalsIgnoreCase("PubDate")) {
	    String pubDate = "";
	    if (DPyear != null) {
		pubDate = DPyear;
		currentArticle.setPYear(Integer.parseInt(DPyear)); // set published year
		DPyear = null;
		if (DPmonth != null) {
		    pubDate += " " + DPmonth;
		    DPmonth = null;
		    if (DPday != null) {
			pubDate += " " + DPday;
			DPday = null;
		    }
		}
	    } else if (MedlineDate != null) {
		pubDate = MedlineDate;
		MedlineDate = null;
	    }
	    currentArticle.setDatePublished(pubDate);
	    inPubDate = inDPDay = inDPMonth = inDPYear = false;
	    // System.out.println("DP = " + currentArticle.getDatePublished());
	} else if (inPubDate && eName.equalsIgnoreCase("Year"))
	    inDPYear = false;
	else if (inPubDate && eName.equalsIgnoreCase("Month"))
	    inDPMonth = false;
	else if (inPubDate && eName.equalsIgnoreCase("Day"))
	    inDPDay = false;
	else if (inMedlineDate && eName.equalsIgnoreCase("MedlineDate"))
	    inMedlineDate = false;
	else if (inIssn && eName.equalsIgnoreCase("issn")) {
	    inIssn = false;
	    currentArticle.setIssn(sbIssn.toString().trim());
	    // System.out.println("PMID = " + currentArticle.getPMID() + ": ISSN = " + currentArticle.getIssn());
	}
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
	if (inID)
	    sbID.append(ch, start, length);
	else if (inTitle)
	    sbTitle.append(ch, start, length);
	else if (inAbstract)
	    sbAbstract.append(ch, start, length);
	else if (inDAYear)
	    DAyear = new String(ch, start, length).trim();
	else if (inDAMonth)
	    DAmonth = new String(ch, start, length).trim();
	else if (inDADay)
	    DAday = new String(ch, start, length).trim();
	else if (inDCOMYear)
	    DCOMyear = new String(ch, start, length).trim();
	else if (inDCOMMonth)
	    DCOMmonth = new String(ch, start, length).trim();
	else if (inDCOMDay)
	    DCOMday = new String(ch, start, length).trim();
	else if (inDPYear)
	    DPyear = new String(ch, start, length).trim();
	else if (inDPMonth)
	    DPmonth = new String(ch, start, length).trim();
	else if (inDPDay)
	    DPday = new String(ch, start, length).trim();
	else if (inMedlineDate)
	    MedlineDate = new String(ch, start, length).trim();
	else if (inIssn) {
	    sbIssn.append(ch, start, length);
	    // System.out.println("In Characters(): ISSN = " + sbIssn.toString());

	}
    }
}
