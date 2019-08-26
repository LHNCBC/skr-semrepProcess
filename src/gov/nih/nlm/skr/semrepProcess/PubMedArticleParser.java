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
 * @author rodriguezal
 *
 */
public class PubMedArticleParser extends DefaultHandler {

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
    private boolean inDCMonth = false;
    private boolean inDCYear = false;
    private boolean inDCDay = false;

    private boolean inEDAT = false;
    private boolean inEDATMonth = false;
    private boolean inEDATYear = false;
    private boolean inEDATDay = false;

    private boolean inIssn = false;

    private StringBuffer sbAbstract;
    private StringBuffer sbTitle;
    private StringBuffer sbAuthor;
    private StringBuffer sbID;
    private StringBuffer sbMetadata;
    private StringBuffer sbIssn;

    private String DCyear;
    private String DCmonth;
    private String DCday;
    private String DCDate;
    private String EDATyear;
    private String EDATmonth;
    private String EDATday;
    private String EDATDate;
    private String DPyear;
    private String DPmonth;
    private String DPday;
    private String DPDate;
    private String MedlineDate = null;
    private String PMID = null;
    boolean PMIDPassed = false;
    boolean ISSNPassed = false;
    int numAbstractText = 0;

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
	    sbID = null;
	    inTitle = false;
	    inAbstract = false;
	    sbAbstract = new StringBuffer(); // moved here because there are multiple AbstractTexts for structured abstract
	    numAbstractText = 0;
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
	    numAbstractText++;
	    String label = attributes.getValue("Label");
	    if (label != null && label.length() > 0)
		sbAbstract.append(label + ": ");
	    // sbAbstract = new StringBuffer();
	} else if (eName.equalsIgnoreCase("PubMedPubDate")) {
	    if (attributes.getValue("PubStatus").equals("entrez")) {
		inEDAT = true;
		// System.out.println("EDAT found for PMID = " + PMID);
	    }
	} else if (inEDAT && eName.equalsIgnoreCase("year"))
	    inEDATYear = true;
	else if (inEDAT && eName.equalsIgnoreCase("month"))
	    inEDATMonth = true;
	else if (inEDAT && eName.equalsIgnoreCase("day"))
	    inEDATDay = true;
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
	    currentArticle.setAbstract(sbAbstract.toString()); // Append multiple AbstractTexts together and save to the article 
	    currentArticle.numAbstractText = numAbstractText;
	    articles.add(currentArticle);
	} else if (eName.equalsIgnoreCase("PMID")) {
	    inID = false;
	    currentArticle.setPMID(sbID.toString().trim());
	    // System.out.println("PMID = " + currentArticle.getPMID());

	} else if (eName.equalsIgnoreCase("articletitle")) {
	    currentArticle.setTitle(sbTitle.toString());
	    inTitle = false;
	} else if (eName.equalsIgnoreCase("abstracttext")) {
	    // currentArticle.setAbstract(sbAbstract.toString());
	    sbAbstract.append(" "); // To prepare another AbstractText to be appended after this
	    inAbstract = false;
	} else if (eName.equalsIgnoreCase("PubMedPubDate")) {
	    inEDAT = inEDATYear = inEDATMonth = inEDATDay = false;
	    StringBuffer EDATBuf = new StringBuffer();
	    if (EDATyear != null) {
		EDATBuf.append(EDATyear + "-");
		EDATyear = null;
		if (EDATmonth != null) {
		    EDATBuf.append(EDATmonth + "-");
		    EDATmonth = null;
		    if (EDATday != null) {
			EDATBuf.append(EDATday);
			EDATday = null;
		    }
		}
	    }
	    if (EDATBuf.length() > 0) {
		currentArticle.setEDAT(EDATBuf.toString());
		// System.out.println("EDAT = " + EDATBuf.toString());
	    }
	} else if (eName.equalsIgnoreCase("pubdate")) {
	    String pubDate = "";
	    if (DPyear != null) {
		pubDate = DPyear;
		try {
		    currentArticle.setPYear(Integer.parseInt(DPyear));
		} catch (Exception e) {
		    e.printStackTrace();
		    DPyear = extractYear(pubDate);
		    System.out.println("PMID = " + currentArticle.getPMID());
		    System.out.println("pubDate = " + pubDate);
		    currentArticle.setPYear(Integer.parseInt(DPyear));

		}
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
		// if (pubDate.length() > 4)
		DPyear = extractYear(pubDate);
		// else
		//     DPyear = pubDate;
		// System.out.println("DPYear = " + DPyear + " from Medline Date = " + pubDate);
		try {
		    currentArticle.setPYear(Integer.parseInt(DPyear));
		    MedlineDate = null;
		} catch (NumberFormatException n) {
		    System.out.println(n.getMessage());
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	    currentArticle.setDatePublished(pubDate);
	    inPubDate = inDPDay = inDPMonth = inDPYear = false;
	    // System.out.println("Date published = " + currentArticle.getDatePublished());

	} else if (inIssn && eName.equalsIgnoreCase("issn")) {
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
	else if (inDPYear) {
	    DPyear = new String(ch, start, length).trim();
	    inDPYear = false;
	} else if (inDPMonth) {
	    DPmonth = new String(ch, start, length).trim();
	    inDPMonth = false;
	} else if (inDPDay) {
	    DPday = new String(ch, start, length).trim();
	    inDPDay = false;
	} else if (inEDATYear) {
	    EDATyear = new String(ch, start, length).trim();
	    // System.out.println("EDAT year = " + EDATyear);
	    inEDATYear = false;
	} else if (inEDATMonth) {
	    EDATmonth = new String(ch, start, length).trim();
	    // System.out.println("EDAT month = " + EDATmonth);
	    inEDATMonth = false;
	} else if (inEDATDay) {
	    EDATday = new String(ch, start, length).trim();
	    inEDATDay = false;
	    // System.out.println("EDAT day = " + EDATday);
	} else if (inMedlineDate) {
	    MedlineDate = new String(ch, start, length).trim();
	    inMedlineDate = false;
	    // System.out.println("MedlineDate detected : " + MedlineDate);
	} else if (inIssn) {
	    sbIssn.append(ch, start, length);
	    // System.out.println("In Characters(): ISSN = " + sbIssn.toString());

	}
    }

    private String extractYear(String date) {
	StringBuffer yearbf = new StringBuffer();
	for (int i = 0; i < date.length(); i++) {
	    if (date.charAt(i) >= '0' && date.charAt(i) <= '9')
		yearbf.append(date.charAt(i));
	    else {
		if (yearbf.length() == 4) // If the length of consecutive digit is four, that is year info and return it
		    return yearbf.toString();
		// else
		//    yearbf = new StringBuffer();
	    }
	}
	// System.out.println("\tdate = " + date + " , year = " + yearbf.toString());
	return yearbf.toString();
    }
}
