package gov.nih.nlm.skr.semrepProcess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nu.xom.Attribute;
/*- import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList; */
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Text;

public class XMLConverter {

    Properties properties = null;
    String semrepSemanticTypeFile = null;
    Map<String, String> SemRepFullTypeMap = new HashMap<>();

    public XMLConverter() throws java.io.IOException {

	properties = FileUtils.loadPropertiesFromFile("semrep.properties");
	semrepSemanticTypeFile = properties.getProperty("SemRepSemanticTypeFile");
	List<String> lines = FileUtils.linesFromFile(semrepSemanticTypeFile, "UTF-8");

	for (String line : lines) {
	    String[] pair = line.split("\\|");
	    SemRepFullTypeMap.put(pair[1].trim(), pair[0].trim());
	}

    }

    public nu.xom.Document changeStructureAndAddSemRepTerms(nu.xom.Document doc, String[] lines) {
	Map<Term, String> TermMap = new HashMap<>();
	Map<String, String> SpanMap = new HashMap<>();
	try {
	    // XPath xPath = XPathFactory.newInstance().newXPath();
	    String sentence = null;
	    String semrepSentence = null;
	    Element rootElem = doc.getRootElement();
	    Attribute textAttr = rootElem.getAttribute("text");
	    String citSource = textAttr.getValue();
	    // Add text element 
	    Element tchild = new Element("text", "");
	    Text textNode = new Text(citSource);

	    tchild.appendChild(textNode);
	    rootElem.removeAttribute(textAttr);
	    rootElem.appendChild(tchild);

	    // remove all the nodes named analysis under sentence

	    String tokenPath = "//token";
	    Nodes nodeList = rootElem.query(tokenPath);
	    // String[] tokens = new String[sentNodeList.getLength()];
	    /*- for (int i = 0; i < sentNodeList.getLength(); i++) {
	    Node sentNode = sentNodeList.item(i);
	    Node analysisNode = sentNode.get;
	    Node tokenizationNode = analysisNode.getFirstChild();
	    doc.renameNode(tokenizationNode, "", "tokens");
	    sentNode.appendChild(tokenizationNode);
	    sentNode.removeChild(analysisNode);
	    } */

	    // NamedNodeMap attrs = rootElem.getAttributes();
	    String tid = null;
	    String eid = null;
	    int idnum = 1;
	    int eventnum = 1;

	    // Variables for adjusting offset from SemRep output to the source used in the parsaer
	    int searchLimit = 0;
	    int citPos = 0;
	    int docOffset = 0;
	    int parOffset = 0;
	    int entityLen = 0;
	    boolean firstEntity = true;
	    int SAOffsetAdjustment = 0;
	    List<Integer> sentOffsetList = new ArrayList<>(); // finding sentence start offsets from the BLLIP XML file

	    // tokenPath = "//sentence";
	    // sentNodeList = (NodeList) xPath.compile(tokenPath).evaluate(doc, XPathConstants.NODESET);
	    // String[] tokens = new String[sentNodeList.getLength()];
	    // for (int i = 0; i < sentNodeList.getLength(); i++) {
	    // Node sentNode = sentNodeList.item(i);
	    Nodes analNodes = rootElem.query("//analyses");
	    for (int index = 0; index < analNodes.size(); index++) {
		Node analNode = analNodes.get(index);
		Node sentNode = analNode.getParent();
		String sentOffsetRange = ((Element) sentNode).getAttribute("charOffset").getValue();
		String[] sentOffsetSplit = sentOffsetRange.split("-");
		int sentStartOffset = Integer.parseInt(sentOffsetSplit[0]); // retrieve sentence start offset to adjust tonek offset
		sentOffsetList.add(sentStartOffset);
		/*- Move sentence text from attribute to a text node
		 * 
		 */
		String sentText = ((Element) sentNode).getAttribute("text").getValue();
		Element sentTextChildNode = new Element("text");
		Text sentTextNode = new Text(sentText);
		sentTextChildNode.appendChild(sentTextNode);
		((Element) sentNode).appendChild(sentTextChildNode);

		/*
		 * sentTextChildNode.appendChild(doc.createTextNode(sentText));
		 * sentNode.appendChild(sentTextChildNode); ((Element)
		 * sentNode).removeAttribute("text"); System.out.println("sentence text = " +
		 * sentText);
		 */

		/*
		 * chnage node name from "tokenization" into tokens
		 * 
		 */
		Elements tokensList = ((Element) analNode).getChildElements("tokenization");
		for (int tindex = 0; tindex < tokensList.size(); tindex++) {
		    Element tokens = tokensList.get(tindex);
		    tokens.setLocalName("tokens");
		    // remove tokenizer attribute
		    Attribute tokenizerAttr = tokens.getAttribute("tokenizer");
		    tokens.removeAttribute(tokenizerAttr);
		    Elements tokenList = tokens.getChildElements("token");
		    for (int tindex2 = 0; tindex2 < tokenList.size(); tindex2++) { // do this for all tokens
			Element token = tokenList.get(tindex2);
			Attribute tokenOffsetAttr = token.getAttribute("charOffset");
			token.removeAttribute(tokenOffsetAttr); // remove previouscharOffset  attribute
			String tokenOffsetRange = tokenOffsetAttr.getValue();
			String[] tokenOffsetSplit = tokenOffsetRange.split("-");
			int tokenStartOffset = Integer.parseInt(tokenOffsetSplit[0]) + sentStartOffset; // adjust token start offset 
			int tokenEndOffset = Integer.parseInt(tokenOffsetSplit[1]) + sentStartOffset; // adjust token start offset 
			String newTokenOffset = new String(tokenStartOffset + "-" + tokenEndOffset);
			token.addAttribute(new Attribute("charOffset", newTokenOffset));
		    }
		    // doc. renameNode(tokens, tokens.getNamespaceURI(), "tokens");
		    ((Element) analNode).removeChild(tokens);
		    ((Element) sentNode).appendChild(tokens); // move "toktnes" node right below "sentence"
		}

		/*
		 * Get the parse tree info from the attribute "pennstring" of the node "parse"
		 * and move that by create a node "tree"
		 */
		Elements parseList = ((Element) analNode).getChildElements("parse");
		for (int pindex = 0; pindex < parseList.size(); pindex++) {
		    Element parseElem = parseList.get(pindex);
		    Attribute pennstringAttr = parseElem.getAttribute("pennstring");
		    String parseText = pennstringAttr.getValue();
		    Element treeTextChildNode = new Element("tree");
		    Text parseTextNode = new Text(parseText);
		    treeTextChildNode.appendChild(parseTextNode);
		    ((Element) sentNode).appendChild(treeTextChildNode);
		    parseElem.removeAttribute(pennstringAttr);
		    parseElem.setLocalName("dependencies");
		    Attribute parserAttr = parseElem.getAttribute("parser");
		    // remove parser attribute in the node named dependencies 
		    parseElem.removeAttribute(parserAttr);
		    Elements phraseList = parseElem.getChildElements("phrase");
		    // remove all phrase nodes below dependencies node
		    for (int ph = 0; ph < phraseList.size(); ph++) {
			Element phraseElem = phraseList.get(ph);
			parseElem.removeChild(phraseElem);
		    }
		    ((Element) analNode).removeChild(parseElem);
		    ((Element) sentNode).appendChild(parseElem);
		    /*- treeTextChildNode.appendChild(doc.createTextNode(parseText));
		    sentNode.appendChild(treeTextChildNode);
		    parseElem.removeAttribute("pennstring");
		    doc.renameNode(parseElem, parseElem.getNamespaceURI(), "dependencies");
		    sentNode.appendChild(parseElem); */
		}
		((Element) sentNode).removeChild(analNode); // remove "analyses" node
	    }

	    /*- System.out.println("analysisNode name = " + analysisNode.getNodeName());
	    Node tokenizationNode = analysisNode.getFirstChild();
	    // doc.renameNode(tokenizationNode, "", "tokens");
	    sentNode.appendChild(tokenizationNode);
	    sentNode.removeChild(analysisNode); */
	    StringBuilder semrepTextSB = new StringBuilder();
	    int semrepOffsetDiff = 0; // calcualted difference of the offset in semrep text
	    int sentNumber = -1;
	    int relationStartOffset = 0;
	    for (String line : lines) {
		if (line.length() > 0) {
		    String compo[] = line.split("\\|");
		    // save text
		    if (compo.length > 5 && compo[5].equals("text")) {
			sentNumber++;
			docOffset = sentOffsetList.get(sentNumber); // retrieve the currect sentence offset
			relationStartOffset = sentOffsetList.get(sentNumber); // retrieve the currect sentence offset and assign to the start offset of the relations in the sentence
			semrepSentence = compo[6];
			semrepTextSB.append(semrepSentence + " ");
			sentence = compo[6].trim().replaceAll("[ ]+", " ");
			System.out.println(sentence);

			int newPos = citSource.indexOf(sentence, citPos);
			if (newPos > citPos)
			    citPos = newPos;
			else if (newPos < 0) {
			    newPos = citSource.indexOf(".", citPos);
			    if (newPos > citPos)
				citPos = newPos;
			    else if (citPos < citSource.length())
				citPos++;
			    else
				break; // Reaching at the end of source citation, so stop processing.
			}
			if (citPos + sentence.length() < citSource.length())
			    searchLimit = citPos + sentence.length();
			else
			    searchLimit = citSource.length(); //Search to the length of the citation
			//			docOffset = citSource.indexOf(sentence,docOffset);
			// System.out.println("DOC OFFSET: " + docOffset);
			// firstEntity = false;
			entityLen = 0;

		    } // Convert SemRep entity into Term
		    else if (compo.length > 8 && compo[5].equals("entity")) {
			if (firstEntity) { // do this for the first entity of the citation to calculate semrepOffsetDiff
			    int sentIndex = semrepTextSB.toString().indexOf(compo[11]);
			    int semrepEntityIndex = Integer.parseInt(compo[16]);
			    semrepOffsetDiff = semrepEntityIndex - sentIndex;
			    firstEntity = false;
			}
			/*- if (firstEntity == false && entityLen == 0)
			    firstEntity = true;
			else
			    firstEntity = false; */
			String semtype = null;
			String typeSrc = compo[8].trim();
			if (typeSrc.contains(",")) {
			    String typeComp[] = typeSrc.split(",");
			    typeSrc = typeComp[0];
			}

			semtype = SemRepFullTypeMap.get(typeSrc);
			tid = new String("T" + idnum);
			idnum++;
			String text = compo[11];
			System.out.println("Semrep token = " + text);

			/*- int startPos = Integer.parseInt(compo[16]);
			int endPos = Integer.parseInt(compo[17]);
			int potentialPos = 0;
			if (firstEntity) {
			    int sentCharOffset = sentence.indexOf(compo[11]);
			    potentialPos = citSource.indexOf(compo[11], docOffset);
			
			    if (potentialPos >= 0) {
				docOffset = citSource.indexOf(compo[11], docOffset) - sentCharOffset;
				parOffset = startPos - sentCharOffset;
				firstEntity = false;
			    } else {
				// SAOffsetAdjustment = SAOffsetAdjustment - 
				continue; // else skip the whole procedure and do not create any TERM child 
			    }
			
			}
			entityLen++;
			
			System.out.println("DOC OFFSET: " + docOffset + ": PAROFFEST " + parOffset);
			int writeStartPos = docOffset + startPos - parOffset; */
			/*- 
			 * 
			 * text utterence may have more than a space in semrep output like "antimicrobial       activity",
			 * so the endPos need to be recalculated. And text utterence need to be extracted from entity info
			*/
			// int writeEndPos = docOffset + endPos - parOffset;
			// 

			/*- String citPart = text.replaceAll("[ ]+", " ");
			int writeEndPos = writeStartPos + citPart.length();
			
			if (citPart.contains("\n")) {
			    System.err.println("HAS NEWLINE " + citPart);
			    int ind = citPart.lastIndexOf("\n") + 1;
			    citPart = citPart.substring(ind);
			    writeStartPos += ind;
			} */
			int tempdocOffset = citSource.indexOf(compo[11], docOffset);
			if (tempdocOffset >= docOffset)
			    docOffset = tempdocOffset;
			else
			    continue;
			int writeStartPos = docOffset;
			int writeEndPos = writeStartPos + compo[11].length();
			String citPart = citSource.substring(writeStartPos, writeEndPos);

			String charOffset = new String(writeStartPos + "-" + writeEndPos);
			System.out.println("charOffset of entity = " + charOffset);
			Element echild = new Element("Term");
			Attribute idAttr = new Attribute("id", tid);
			echild.addAttribute(idAttr);
			Attribute semtypeAttr = new Attribute("type", semtype);
			echild.addAttribute(semtypeAttr);
			Attribute coffsetAttr = new Attribute("charOffset", charOffset);
			echild.addAttribute(coffsetAttr);
			Attribute hoffsetAttr = new Attribute("headOffset", charOffset);
			echild.addAttribute(hoffsetAttr);
			Attribute indicatorAttr = new Attribute("text", citPart);
			echild.addAttribute(indicatorAttr);

			rootElem.appendChild(echild);
			Term newT = new Term(charOffset, semtype);
			TermMap.put(newT, tid);

		    } else if (compo.length > 8 && compo[5].equals("relation")) {
			tid = new String("T" + idnum);
			idnum++;
			String indType = compo[21];
			String subjText = compo[14];
			String objText = compo[34];

			int bestGuessOffset = citSource.indexOf(subjText, relationStartOffset);
			if (bestGuessOffset < 0)
			    continue;

			int startIndexSubj = bestGuessOffset;
			int endIndexSubj = startIndexSubj + compo[13].length();

			int startPredPos = Integer.parseInt(compo[24]) - semrepOffsetDiff;
			int endPredPos = Integer.parseInt(compo[25]) - semrepOffsetDiff;
			String textSoFar = semrepTextSB.toString();
			String citPart = semrepTextSB.toString().substring(startPredPos, endPredPos);

			bestGuessOffset = citSource.indexOf(objText, relationStartOffset);
			if (bestGuessOffset < 0)
			    continue;

			int startIndexObj = bestGuessOffset;
			int endIndexObj = startIndexObj + compo[13].length();

			/*- int startIndexSubj = Integer.parseInt(compo[19]);
			int endIndexSubj = Integer.parseInt(compo[20]);
			int startIndexObj = Integer.parseInt(compo[39]);
			int endIndexObj = Integer.parseInt(compo[40]);
			
			int startIndexPred = Integer.parseInt(compo[24]);
			int endIndexPred = Integer.parseInt(compo[25]);
			String predText = findPredUsingReferenceString(startIndexSubj, endIndexSubj, subjText,
				startIndexPred, endIndexPred, startIndexObj, endIndexObj, objText, sentence);
				*/
			String charOffset = new String(startPredPos + "-" + endPredPos);
			System.out.println("charOffset of relation : " + charOffset + "\t: line : " + line);
			// String citPart = semrepSentence.substring(startPos - parOffset, endPos - parOffset);
			// String citPart = citSource.substring(writeStartPos, writeEndPos);
			/*- if (citPart.contains("\n")) {
			    System.err.println("HAS NEWLINE " + citPart);
			    int ind = citPart.lastIndexOf("\n") + 1;
			    citPart = citPart.substring(ind);
			    writeStartPos += ind;
			} */

			/*- int offsetDiff = startPos - writeStartPos;
			int startIndexSubj = Integer.parseInt(compo[19]) - offsetDiff;
			int endIndexSubj = Integer.parseInt(compo[20]) - offsetDiff;
			int startIndexObj = Integer.parseInt(compo[39]) - offsetDiff;
			int endIndexObj = Integer.parseInt(compo[40]) - offsetDiff; */
			Element echild = new Element("Term");

			Attribute idAttr = new Attribute("id", tid);
			echild.addAttribute(idAttr);
			Attribute semtypeAttr = new Attribute("type", compo[22]);
			echild.addAttribute(semtypeAttr);
			Attribute coffsetAttr = new Attribute("charOffset", charOffset);
			System.out.println("charOffset of relation : " + charOffset + "\t: line : " + line);
			echild.addAttribute(coffsetAttr);
			Attribute hoffsetAttr = new Attribute("headOffset", charOffset);
			echild.addAttribute(hoffsetAttr);
			Attribute indicatorAttr = new Attribute("text", citPart);
			echild.addAttribute(indicatorAttr);

			/*- echild.setAttribute("id", tid);
			echild.setAttribute("type", compo[22]);
			echild.setAttribute("charOffset", charOffset);
			echild.setAttribute("headOffset", charOffset);
			echild.setAttribute("text", citPart); */

			rootElem.appendChild(echild);
			Term subjT = new Term(new String(startIndexSubj + "-" + endIndexSubj), compo[11]);
			String subjTermRef = TermMap.get(subjT);
			Term objT = new Term(new String(startIndexObj + "-" + endIndexObj), compo[31]);
			String objTermRef = TermMap.get(objT);
			// Element eventchild = new Element("Event");
			// 09/11/2018
			// New BoiScores creates Predication element instead of Event
			Element eventchild = new Element("Predication");
			eid = new String("E" + eventnum);
			eventnum++;
			Attribute id2Attr = new Attribute("id", eid);
			eventchild.addAttribute(id2Attr);
			Attribute typeAttr = new Attribute("type", compo[22]);
			eventchild.addAttribute(typeAttr);
			Attribute predAttr = new Attribute("predicate", tid);
			eventchild.addAttribute(predAttr);
			Attribute indAttr = new Attribute("indicatorType", compo[21]);
			eventchild.addAttribute(indAttr);

			Element subjchild = new Element("Subject");
			Attribute subjAttr = new Attribute("idref", subjTermRef);
			subjchild.addAttribute(subjAttr);
			Element objchild = new Element("Object");
			Attribute objAttr = new Attribute("idref", objTermRef);
			objchild.addAttribute(objAttr);

			/*- eventchild.setAttribute("type", compo[22]);
			eventchild.setAttribute("predicate", tid);
			eventchild.setAttribute("indicatorType", compo[21]);
			Element subjchild = doc.createElement("Subject");
			subjchild.setAttribute("idref", subjTermRef);
			eventchild.appendChild(subjchild);
			
			Element objchild = doc.createElement("Object");
			objchild.setAttribute("idref", objTermRef); */
			eventchild.appendChild(subjchild);
			eventchild.appendChild(objchild);
			rootElem.appendChild(eventchild);
		    }
		}
	    }

	} catch (

	Exception e) {
	    e.printStackTrace();
	}

	return doc;
    }

    public nu.xom.Document addSemRepTerms(nu.xom.Document doc, List<String> lines) {
	Map<Term, String> TermMap = new HashMap<>();
	Map<String, String> SpanMap = new HashMap<>();
	try {
	    // XPath xPath = XPathFactory.newInstance().newXPath();
	    String sentence = null;
	    String semrepSentence = null;
	    Element rootElem = doc.getRootElement();
	    Elements textList = rootElem.getChildElements("text");
	    Element textNode = textList.get(0);
	    String citSource = textNode.getValue();
	    //  Attribute textAttr = rootElem.getAttribute("text");
	    // String citSource = textAttr.getValue();
	    // NamedNodeMap attrs = rootElem.getAttributes();
	    String tid = null;
	    String eid = null;
	    int idnum = 1;
	    int eventnum = 1;

	    // Variables for adjusting offset from SemRep output to the source used in the parsaer
	    int searchLimit = 0;
	    int citPos = 0;
	    int docOffset = 0;
	    int parOffset = 0;
	    int entityLen = 0;
	    boolean firstEntity = true;
	    int SAOffsetAdjustment = 0;
	    List<Integer> sentOffsetList = new ArrayList<>(); // finding sentence start offsets from the BLLIP XML file

	    StringBuilder semrepTextSB = new StringBuilder();
	    int semrepOffsetDiff = 0; // calcualted difference of the offset in semrep text
	    int sentNumber = -1;
	    int relationStartOffset = 0;
	    for (String line : lines) {
		if (line.length() > 0) {
		    String compo[] = line.split("\\|");
		    if (compo.length > 8 && compo[5].equals("entity")) {
			if (firstEntity) { // do this for the first entity of the citation to calculate semrepOffsetDiff
			    int sentIndex = semrepTextSB.toString().indexOf(compo[11]);
			    int semrepEntityIndex = Integer.parseInt(compo[16]);
			    semrepOffsetDiff = semrepEntityIndex - sentIndex;
			    firstEntity = false;
			}
			/*- if (firstEntity == false && entityLen == 0)
			    firstEntity = true;
			else
			    firstEntity = false; */
			String semtype = null;
			String typeSrc = compo[8].trim();
			if (typeSrc.contains(",")) {
			    String typeComp[] = typeSrc.split(",");
			    typeSrc = typeComp[0];
			} else if (typeSrc.equals("")) { // Ignoring SemRep 1.8 error when entity type is empty
			    continue;
			}

			semtype = SemRepFullTypeMap.get(typeSrc);
			tid = new String("T" + idnum);
			idnum++;
			String text = compo[11];
			String CUI = compo[6];
			if (CUI.length() <= 0) // If CUI is empty, take Entrez gene ID instead
			    CUI = compo[9];
			String name = compo[7];
			String type = compo[8];
			// System.out.println("Semrep token = " + text);
			String charOffset = null;
			/*
			 * Oct 4 2018 If the start offset is less that or equal to end offset of an
			 * entity, add charOffset as "start-end", otherwise charOffset = "end-start"
			 */
			if (Integer.parseInt(compo[16]) <= Integer.parseInt(compo[17]))
			    charOffset = new String(compo[16] + "-" + compo[17]);
			else
			    charOffset = new String(compo[17] + "-" + compo[16]);
			// System.out.println("charOffset of entity = " + charOffset);
			Element echild = new Element("Term");
			Attribute idAttr = new Attribute("id", tid);
			echild.addAttribute(idAttr);
			Attribute semtypeAttr = new Attribute("type", semtype);
			echild.addAttribute(semtypeAttr);
			Attribute coffsetAttr = new Attribute("charOffset", charOffset);
			echild.addAttribute(coffsetAttr);
			Attribute hoffsetAttr = new Attribute("headOffset", charOffset);
			echild.addAttribute(hoffsetAttr);
			Attribute indicatorAttr = new Attribute("text", compo[11]);
			echild.addAttribute(indicatorAttr);

			Element cchild = new Element("Concept");
			Attribute CuiAttr = null;
			CuiAttr = new Attribute("id", CUI);
			cchild.addAttribute(CuiAttr);

			Attribute nameAttr = null;
			if (name.length() > 0)
			    nameAttr = new Attribute("name", name);
			else
			    nameAttr = new Attribute("name", compo[10]);
			cchild.addAttribute(nameAttr);
			Attribute semtypeAttr2 = new Attribute("semtypes", semtype);
			cchild.addAttribute(semtypeAttr2);
			Attribute sense = new Attribute("sense", "true");
			cchild.addAttribute(sense);

			echild.appendChild(cchild);
			rootElem.appendChild(echild);
			Term term = new Term(charOffset, CUI);
			TermMap.put(term, tid);
			// If Term for this entity does not match, then we need to use just Span instead
			SpanMap.put(charOffset, tid);

		    } else if (compo.length > 8 && compo[5].equals("relation")) {
			tid = new String("T" + idnum);
			idnum++;
			String indType = compo[21];
			String subjText = compo[14];
			String objText = compo[34];
			String subjCUI = compo[8];
			if (subjCUI.length() <= 0) // If subject CUI is empty, use the entrezgene ID instead
			    subjCUI = compo[12];

			String objCUI = compo[28];
			if (objCUI.length() <= 0) // If subject CUI is empty, use the entrezgene ID instead
			    objCUI = compo[32];

			String subjType = compo[10];
			String objType = compo[31];
			String predicate = compo[22];
			String specInfer = null;
			boolean hasSpecInfer = false;

			int startIndexSubj = Integer.parseInt(compo[19]);
			int endIndexSubj = Integer.parseInt(compo[20]);

			int startPredPos = Integer.parseInt(compo[24]);
			int endPredPos = Integer.parseInt(compo[25]);
			String textSoFar = semrepTextSB.toString();
			String citPart = null;
			String charOffset = null;
			if (startPredPos <= endPredPos) {
			    citPart = citSource.substring(startPredPos, endPredPos);
			    charOffset = new String(startPredPos + "-" + endPredPos);
			} else {
			    citPart = citSource.substring(endPredPos, startPredPos);
			    charOffset = new String(endPredPos + "-" + startPredPos);
			}

			int startIndexObj = Integer.parseInt(compo[39]);
			int endIndexObj = Integer.parseInt(compo[40]);

			// System.out.println("charOffset of relation : " + charOffset + "\t: line : " + line);

			Element echild = new Element("Term");

			Attribute idAttr = new Attribute("id", tid);
			echild.addAttribute(idAttr);
			/*
			 * If a predicate has SPEC or INFER like PROCESS_OF(SPEC), type attribute needs
			 * to be "PROCESS_OF" specInfer attribute needs to be "SPEC" in XML output in
			 * Predication element
			 */
			if (predicate.contains("(")) {
			    predicate = predicate.substring(0, predicate.indexOf("("));
			    specInfer = predicate.substring(predicate.indexOf("(") + 1);
			    hasSpecInfer = true;
			}
			Attribute semtypeAttr = new Attribute("type", predicate);
			echild.addAttribute(semtypeAttr);
			Attribute coffsetAttr = new Attribute("charOffset", charOffset);
			// System.out.println("charOffset of relation : " + charOffset + "\t: line : " + line);
			echild.addAttribute(coffsetAttr);
			Attribute hoffsetAttr = new Attribute("headOffset", charOffset);
			echild.addAttribute(hoffsetAttr);
			Attribute indicatorAttr = new Attribute("text", citPart);
			echild.addAttribute(indicatorAttr);

			rootElem.appendChild(echild);

			Term subjT = new Term(new String(startIndexSubj + "-" + endIndexSubj), subjCUI);
			String subjTermRef = TermMap.get(subjT);
			// If the Term for this argument does not match with any entity, then we need to use just Span instead
			// because occasionally an argument is represented by GeneId, while entity is represented by CUI
			if (subjTermRef == null)
			    subjTermRef = SpanMap.get(new String(startIndexSubj + "-" + endIndexSubj));
			Term objT = new Term(new String(startIndexObj + "-" + endIndexObj), objCUI);
			String objTermRef = TermMap.get(objT);
			if (objTermRef == null)
			    objTermRef = SpanMap.get(new String(startIndexObj + "-" + endIndexObj));
			// Element eventchild = new Element("Event");
			// 09/11/2018
			// Event element is changed into Predication
			Element eventchild = new Element("Predication");
			eid = new String("E" + eventnum);
			eventnum++;
			Attribute id2Attr = new Attribute("id", eid);
			eventchild.addAttribute(id2Attr);
			Attribute typeAttr = new Attribute("type", predicate);
			eventchild.addAttribute(typeAttr);
			Attribute predAttr = new Attribute("predicate", tid);
			eventchild.addAttribute(predAttr);
			Attribute indAttr = new Attribute("indicatorType", compo[21]);
			eventchild.addAttribute(indAttr);
			if (hasSpecInfer) {
			    Attribute inferAttr = new Attribute("indicatorType", specInfer);
			    eventchild.addAttribute(inferAttr);
			}

			Element subjchild = new Element("Subject");
			Attribute subjAttr = new Attribute("idref", subjTermRef);
			subjchild.addAttribute(subjAttr);
			Element objchild = new Element("Object");
			Attribute objAttr = new Attribute("idref", objTermRef);
			objchild.addAttribute(objAttr);

			/*- eventchild.setAttribute("type", compo[22]);
			eventchild.setAttribute("predicate", tid);
			eventchild.setAttribute("indicatorType", compo[21]);
			Element subjchild = doc.createElement("Subject");
			subjchild.setAttribute("idref", subjTermRef);
			eventchild.appendChild(subjchild);
			
			Element objchild = doc.createElement("Object");
			objchild.setAttribute("idref", objTermRef); */
			eventchild.appendChild(subjchild);
			eventchild.appendChild(objchild);
			rootElem.appendChild(eventchild);
		    }
		}
	    }

	} catch (

	Exception e) {
	    e.printStackTrace();
	}

	return doc;
    }

    /*
     * Use new MEDLINE format and NORMALIZED format The difference of offset is:
     * Offset of SemRep output should be -12 for title, and -18 for abstract
     */
    public nu.xom.Document addSemRepTerms2019(nu.xom.Document doc, List<String> lines) {
	Map<Term, String> TermMap = new HashMap<>();
	Map<String, String> SpanMap = new HashMap<>();
	try {
	    // XPath xPath = XPathFactory.newInstance().newXPath();
	    String sentence = null;
	    String semrepSentence = null;
	    Element rootElem = doc.getRootElement();
	    Elements textList = rootElem.getChildElements("text");
	    Element textNode = textList.get(0);
	    String citSource = textNode.getValue();
	    //  Attribute textAttr = rootElem.getAttribute("text");
	    // String citSource = textAttr.getValue();
	    // NamedNodeMap attrs = rootElem.getAttributes();
	    String tid = null;
	    String eid = null;
	    int idnum = 1;
	    int eventnum = 1;

	    // Variables for adjusting offset from SemRep output to the source used in the parsaer
	    int searchLimit = 0;
	    int citPos = 0;
	    int docOffset = 0;
	    int parOffset = 0;
	    int entityLen = 0;
	    boolean firstEntity = true;
	    int SAOffsetAdjustment = 0;
	    List<Integer> sentOffsetList = new ArrayList<>(); // finding sentence start offsets from the BLLIP XML file

	    StringBuilder semrepTextSB = new StringBuilder();
	    int semrepOffsetDiff = 0; // calcualted difference of the offset in semrep text
	    int sentNumber = -1;
	    int relationStartOffset = 0;
	    for (String line : lines) {
		if (line.length() > 0) {
		    String compo[] = line.split("\\|");
		    if (compo.length > 8 && compo[5].equals("entity")) {
			if (firstEntity) { // do this for the first entity of the citation to calculate semrepOffsetDiff
			    int sentIndex = semrepTextSB.toString().indexOf(compo[11]);
			    int semrepEntityIndex = Integer.parseInt(compo[16]);
			    semrepOffsetDiff = semrepEntityIndex - sentIndex;
			    firstEntity = false;
			}
			/*- if (firstEntity == false && entityLen == 0)
			    firstEntity = true;
			else
			    firstEntity = false; */
			String semtype = null;
			String typeSrc = compo[8].trim();
			if (typeSrc.contains(",")) {
			    String typeComp[] = typeSrc.split(",");
			    typeSrc = typeComp[0];
			} else if (typeSrc.equals("")) { // Ignoring SemRep 1.8 error when entity type is empty
			    continue;
			}

			semtype = SemRepFullTypeMap.get(typeSrc);
			tid = new String("T" + idnum);
			idnum++;
			String text = compo[11];
			String CUI = compo[6];
			if (CUI.length() <= 0) // If CUI is empty, take Entrez gene ID instead
			    CUI = compo[9];
			String name = compo[7];
			String type = compo[8];
			// System.out.println("Semrep token = " + text);
			String charOffset = null;
			/*
			 * Jan 3 2019, offset needs to be recalculated depending on whether the entity
			 * is in title or abstract
			 */
			int revisedStartIndex = 0;
			int revisedEndIndex = 0;
			if (compo[3].equals("ti")) {
			    revisedStartIndex = Integer.parseInt(compo[16]) - 12;
			    revisedEndIndex = Integer.parseInt(compo[17]) - 12;
			} else if (compo[3].equals("ab")) {
			    revisedStartIndex = Integer.parseInt(compo[16]) - 18;
			    revisedEndIndex = Integer.parseInt(compo[17]) - 18;
			}
			/*
			 * Oct 4 2018 If the start offset is less that or equal to end offset of an
			 * entity, add charOffset as "start-end", otherwise charOffset = "end-start"
			 */
			/*- if (Integer.parseInt(compo[16]) <= Integer.parseInt(compo[17]))
			    charOffset = new String(compo[16] + "-" + compo[17]);
			else
			    charOffset = new String(compo[17] + "-" + compo[16]); */
			if (revisedStartIndex > revisedEndIndex) {
			    int temp = revisedStartIndex;
			    revisedStartIndex = revisedEndIndex;
			    revisedEndIndex = temp;
			}
			charOffset = new String(
				String.valueOf(revisedStartIndex) + "-" + String.valueOf(revisedEndIndex));
			// System.out.println("charOffset of entity = " + charOffset);
			Element echild = new Element("Term");
			Attribute idAttr = new Attribute("id", tid);
			echild.addAttribute(idAttr);
			Attribute semtypeAttr = new Attribute("type", semtype);
			echild.addAttribute(semtypeAttr);
			Attribute coffsetAttr = new Attribute("charOffset", charOffset);
			echild.addAttribute(coffsetAttr);
			Attribute hoffsetAttr = new Attribute("headOffset", charOffset);
			echild.addAttribute(hoffsetAttr);
			Attribute indicatorAttr = new Attribute("text", compo[11]);
			echild.addAttribute(indicatorAttr);

			Element cchild = new Element("Concept");
			Attribute CuiAttr = null;
			CuiAttr = new Attribute("id", CUI);
			cchild.addAttribute(CuiAttr);

			Attribute nameAttr = null;
			if (name.length() > 0)
			    nameAttr = new Attribute("name", name);
			else
			    nameAttr = new Attribute("name", compo[10]);
			cchild.addAttribute(nameAttr);
			Attribute semtypeAttr2 = new Attribute("semtypes", semtype);
			cchild.addAttribute(semtypeAttr2);
			Attribute sense = new Attribute("sense", "true");
			cchild.addAttribute(sense);

			echild.appendChild(cchild);
			rootElem.appendChild(echild);
			Term term = new Term(charOffset, CUI);
			TermMap.put(term, tid);
			// If Term for this entity does not match, then we need to use just Span instead
			SpanMap.put(charOffset, tid);

		    } else if (compo.length > 8 && compo[5].equals("relation")) {
			tid = new String("T" + idnum);
			idnum++;
			String indType = compo[21];
			String subjText = compo[14];
			String objText = compo[34];
			String subjCUI = compo[8];
			if (subjCUI.length() <= 0) // If subject CUI is empty, use the entrezgene ID instead
			    subjCUI = compo[12];

			String objCUI = compo[28];
			if (objCUI.length() <= 0) // If subject CUI is empty, use the entrezgene ID instead
			    objCUI = compo[32];

			String subjType = compo[10];
			String objType = compo[31];
			String predicate = compo[22];
			String specInfer = null;
			boolean hasSpecInfer = false;

			int startIndexSubj = Integer.parseInt(compo[19]);
			int endIndexSubj = Integer.parseInt(compo[20]);

			int startPredPos = Integer.parseInt(compo[24]);
			int endPredPos = Integer.parseInt(compo[25]);
			String textSoFar = semrepTextSB.toString();
			String citPart = null;
			String charOffset = null;

			int startIndexObj = Integer.parseInt(compo[39]);
			int endIndexObj = Integer.parseInt(compo[40]);

			int revisedStartIndexSubj = 0;
			int revisedEndIndexSubj = 0;
			int revisedStartIndexObj = 0;
			int revisedEndIndexObj = 0;
			int revisedStartPredPos = 0;
			int revisedEndPredPos = 0;

			if (compo[3].equals("ti")) {
			    revisedStartIndexSubj = startIndexSubj - 12;
			    revisedEndIndexSubj = endIndexSubj - 12;
			} else if (compo[3].equals("ab")) {
			    revisedStartIndexSubj = startIndexSubj - 18;
			    revisedEndIndexSubj = endIndexSubj - 18;
			}

			if (compo[3].equals("ti")) {
			    revisedStartPredPos = startPredPos - 12;
			    revisedEndPredPos = endPredPos - 12;
			} else if (compo[3].equals("ab")) {
			    revisedStartPredPos = startPredPos - 18;
			    revisedEndPredPos = endPredPos - 18;
			}

			if (compo[3].equals("ti")) {
			    revisedStartIndexObj = startIndexObj - 12;
			    revisedEndIndexObj = endIndexObj - 12;
			} else if (compo[3].equals("ab")) {
			    revisedStartIndexObj = startIndexObj - 18;
			    revisedEndIndexObj = endIndexObj - 18;
			}

			if (revisedStartPredPos <= revisedEndPredPos) {
			    citPart = citSource.substring(revisedStartPredPos, revisedEndPredPos);
			    charOffset = new String(revisedStartPredPos + "-" + revisedEndPredPos);
			} else {
			    citPart = citSource.substring(revisedEndPredPos, revisedStartPredPos);
			    charOffset = new String(revisedEndPredPos + "-" + revisedStartPredPos);
			}

			// System.out.println("charOffset of relation : " + charOffset + "\t: line : " + line);

			Element echild = new Element("Term");

			Attribute idAttr = new Attribute("id", tid);
			echild.addAttribute(idAttr);
			/*
			 * If a predicate has SPEC or INFER like PROCESS_OF(SPEC), type attribute needs
			 * to be "PROCESS_OF" specInfer attribute needs to be "SPEC" in XML output in
			 * Predication element
			 */
			if (predicate.contains("(")) {
			    predicate = predicate.substring(0, predicate.indexOf("("));
			    specInfer = predicate.substring(predicate.indexOf("(") + 1);
			    hasSpecInfer = true;
			}
			Attribute semtypeAttr = new Attribute("type", predicate);
			echild.addAttribute(semtypeAttr);
			Attribute coffsetAttr = new Attribute("charOffset", charOffset);
			// System.out.println("charOffset of relation : " + charOffset + "\t: line : " + line);
			echild.addAttribute(coffsetAttr);
			Attribute hoffsetAttr = new Attribute("headOffset", charOffset);
			echild.addAttribute(hoffsetAttr);
			Attribute indicatorAttr = new Attribute("text", citPart);
			echild.addAttribute(indicatorAttr);

			rootElem.appendChild(echild);

			Term subjT = new Term(new String(revisedStartIndexSubj + "-" + revisedEndIndexSubj), subjCUI);
			String subjTermRef = TermMap.get(subjT);
			// If the Term for this argument does not match with any entity, then we need to use just Span instead
			// because occasionally an argument is represented by GeneId, while entity is represented by CUI
			if (subjTermRef == null)
			    subjTermRef = SpanMap.get(new String(revisedStartIndexSubj + "-" + revisedEndIndexSubj));
			Term objT = new Term(new String(revisedStartIndexObj + "-" + revisedEndIndexObj), objCUI);
			String objTermRef = TermMap.get(objT);
			if (objTermRef == null)
			    objTermRef = SpanMap.get(new String(revisedStartIndexObj + "-" + revisedEndIndexObj));
			// Element eventchild = new Element("Event");
			// 09/11/2018
			// Event element is changed into Predication
			Element eventchild = new Element("Predication");
			eid = new String("E" + eventnum);
			eventnum++;
			Attribute id2Attr = new Attribute("id", eid);
			eventchild.addAttribute(id2Attr);
			Attribute typeAttr = new Attribute("type", predicate);
			eventchild.addAttribute(typeAttr);
			Attribute predAttr = new Attribute("predicate", tid);
			eventchild.addAttribute(predAttr);
			Attribute indAttr = new Attribute("indicatorType", compo[21]);
			eventchild.addAttribute(indAttr);
			if (hasSpecInfer) {
			    Attribute inferAttr = new Attribute("indicatorType", specInfer);
			    eventchild.addAttribute(inferAttr);
			}

			Element subjchild = new Element("Subject");
			Attribute subjAttr = new Attribute("idref", subjTermRef);
			subjchild.addAttribute(subjAttr);
			Element objchild = new Element("Object");
			Attribute objAttr = new Attribute("idref", objTermRef);
			objchild.addAttribute(objAttr);

			/*- eventchild.setAttribute("type", compo[22]);
			eventchild.setAttribute("predicate", tid);
			eventchild.setAttribute("indicatorType", compo[21]);
			Element subjchild = doc.createElement("Subject");
			subjchild.setAttribute("idref", subjTermRef);
			eventchild.appendChild(subjchild);
			
			Element objchild = doc.createElement("Object");
			objchild.setAttribute("idref", objTermRef); */
			eventchild.appendChild(subjchild);
			eventchild.appendChild(objchild);
			rootElem.appendChild(eventchild);
		    }
		}
	    }

	} catch (

	Exception e) {
	    e.printStackTrace();
	}

	return doc;
    }

    public String findPredUsingReferenceString(int startIndexSubj, int endIndexSubj, String subjText,
	    int startIndexPred, int endIndexPred, int startIndexObj, int endIndexObj, String objText, String sentence) {
	int subjObjDiff = startIndexObj - startIndexSubj;
	int curIndex = 0;
	String predText = null;
	while (curIndex < sentence.length()) {
	    int firstIndex = sentence.indexOf(subjText, curIndex);
	    int guessedObjStartIndex = firstIndex + subjObjDiff;
	    String objCandidate = sentence.substring(guessedObjStartIndex,
		    guessedObjStartIndex + endIndexObj - startIndexObj);
	    if (objText.equals(objCandidate)) { // the subject and object candidate are right, so use the founded index in order to extract predicate text
		int semrepIndexDiff = startIndexSubj - firstIndex;
		int realStartIndexPred = startIndexPred - semrepIndexDiff;
		predText = sentence.substring(realStartIndexPred, realStartIndexPred + endIndexPred - startIndexPred);
		return predText;
	    }
	    curIndex = firstIndex + 1;

	}
	return new String("Predicate Not Found");

    }

    class Term {
	String offset;
	String id;

	Term(String offset, String id) {
	    this.offset = offset;
	    this.id = id;
	}

	@Override
	public boolean equals(Object o) {
	    Term t = (Term) o;
	    return this.offset.equals(t.offset) && this.id.equals(t.id);
	}

	@Override
	public int hashCode() {
	    return this.offset.hashCode() + 13 * this.id.hashCode();
	}
    }
}
