package gov.nih.nlm.skr.semrepProcess;

public class testReplace_UTF8 {

    static public void main(String args[]) {
	try {
	    // String in = "˙OH-";
	    String in = "In this study, a series of coumarin-3-acylamino derivatives containing phenethylamine moiety or tyramine moiety were synthesized and their antioxidant activities were evaluated by Cu(2+)/glutathione(GSH)-, ˙OH- and 2,2'-azobis(2-amidinopropane hydrochloride)(AAPH)-induced oxidation of DNA. It was found that both hydroxyl and ortho-methoxy groups at A ring, hydroxyl group at B ring and peptide bond can enhance the abilities of coumarin-3-acylamino derivatives to protect DNA against ˙OH- and AAPH-induced oxidation. Moreover, these coumarin-3-acylamino derivatives were employed to scavenge 2,2'-azinobis(3-ethylbenzothiazoline-6-sulfonate) cationic radical (ABTS(+˙)). We found that tyramine moiety, hydroxyl and ortho-methoxy are the key groups to enhance the activities of antioxidants to quench ABTS(+˙). Therefore, tyramine linked with coumarin-3-carboxyl acid which containing hydroxyl and ortho-methoxy exhibited powerful antioxidant abilities.\r\n";
	    in = "In this study, a series of coumarin-3-acylamino derivatives containing phenethylamine moiety or tyramine moiety were synthesized and their antioxidant activities were evaluated by Cu(2+)/glutathione(GSH)-, ?OH- and 2,2'-azobis(2-amidinopropane hydrochloride)(AAPH)-induced oxidation of DNA. It was found that both hydroxyl and ortho-methoxy groups at A ring, hydroxyl group at B ring and peptide bond can enhance the abilities of coumarin-3-acylamino derivatives to protect DNA against ?OH- and AAPH-induced oxidation. Moreover, these coumarin-3-acylamino derivatives were employed to scavenge 2,2'-azinobis(3-ethylbenzothiazoline-6-sulfonate) cationic radical (ABTS(+?)). We found that tyramine moiety, hydroxyl and ortho-methoxy are the key groups to enhance the activities of antioxidants to quench ABTS(+?). Therefore, tyramine linked with coumarin-3-carboxyl acid which containing hydroxyl and ortho-methoxy exhibited powerful antioxidant abilities.</AbstractText>\r\n";

	    String out = replace_UTF8.ReplaceLooklike(in);
	    System.out.println(out);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
