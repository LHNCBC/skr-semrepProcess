package gov.nih.nlm.skr.semrepProcess;

public class PubmedArticle {
    //	private static Log log = LogFactory.getLog(PubmedArticle.class);

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public String PMID;
    public String datePublished;
    public String dateCreated;
    public String EDAT;
    public String issn;
    public int PYear;
    public String title;
    public String ab;
    public int numAbstractText;

    public PubmedArticle() {
    }

    /**
     *
     */
    public PubmedArticle(String id, String pubDate, String creDate, String edat) {
	this.PMID = id;
	this.datePublished = pubDate;
	this.dateCreated = creDate;
	this.EDAT = edat;
    }

    public PubmedArticle(String id, String pubDate, String creDate, String edat, int pyear) {
	this.PMID = id;
	this.datePublished = pubDate;
	this.dateCreated = creDate;
	this.EDAT = edat;
	this.PYear = pyear;
    }

    /**
     * @return Returns the pubDate.
     */
    public String getPMID() {
	return PMID;
    }

    public void setPMID(String id) {
	this.PMID = id;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public String getAbstract() {
	return ab;
    }

    public void setAbstract(String ab) {
	this.ab = ab;
    }

    public void setDateCreated(String date) {
	dateCreated = date;
    }

    public String getDateCreated() {
	return dateCreated;
    }

    public void setEDAT(String date) {
	EDAT = date;
    }

    public String getEDAT() {
	return EDAT;
    }

    public void setDatePublished(String pubDate) {
	datePublished = pubDate;
    }

    public String getDatePublished() {
	return datePublished;
    }

    public void setIssn(String in) {
	issn = in;
    }

    public String getIssn() {
	return issn;
    }

    public void setPYear(int pyear) {
	this.PYear = pyear;
    }

    public int getPYear() {
	return PYear;
    }
}
