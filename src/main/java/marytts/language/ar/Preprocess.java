package marytts.language.ar;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.ibm.icu.util.ULocale;

import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.datatypes.MaryXML;
import marytts.modules.InternalModule;
import marytts.util.dom.MaryDomUtils;
import marytts.util.dom.NameNodeFilter;

import java.io.*;
import java.net.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import com.ibm.icu.text.RuleBasedNumberFormat;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

/**
 * @author Tristan Hamilton + HB
 * 
 *         Processes cardinal and ordinal numbers.
 */
public class Preprocess extends InternalModule {

    private RuleBasedNumberFormat rbnf;
    protected final String cardinalRule;
    //protected final String ordinalRule;

    public Preprocess() {
	super("Preprocess", MaryDataType.TOKENS, MaryDataType.WORDS, new Locale("ar"));
	this.rbnf = new RuleBasedNumberFormat(new ULocale("ar"), RuleBasedNumberFormat.SPELLOUT);
	this.cardinalRule = "%spellout-numbering";
	//this.ordinalRule = getOrdinalRuleName(rbnf);
    }

    public MaryData process(MaryData d) throws Exception {
	Document doc = d.getDocument();
	logger.info("preprocess 'ar': calling checkForNumbers");
	checkForNumbers(doc);
	vocaliseDoc(doc);
	MaryData result = new MaryData(getOutputType(), d.getLocale());
	result.setDocument(doc);
	return result;
    }

    protected void vocaliseDoc(Document doc) throws Exception {
	TreeWalker tw = ((DocumentTraversal) doc).createTreeWalker(doc, NodeFilter.SHOW_ELEMENT,
								   new NameNodeFilter(MaryXML.TOKEN), false);
	Element t = null;
        StringBuilder origText = new StringBuilder();
	while ((t = (Element) tw.nextNode()) != null) {
	    //if (MaryDomUtils.hasAncestor(t, MaryXML.SAYAS) || t.hasAttribute("ph") || t.hasAttribute("sounds_like")) {
		// ignore token
	    //continue;
	    origText.append(" " + MaryDomUtils.tokenText(t));
	}
	String vocText = vocaliseText(origText.toString());
	//String vocText = vocaliseTextMishkal(origText.toString());

	String[] vocTextList = vocText.split(" ");


	TreeWalker tw2 = ((DocumentTraversal) doc).createTreeWalker(doc, NodeFilter.SHOW_ELEMENT,
								   new NameNodeFilter(MaryXML.TOKEN), false);
	Element t2 = null;
	int i = 0;

	while ((t2 = (Element) tw2.nextNode()) != null) {
	    MaryDomUtils.setTokenText(t2, vocTextList[i]);
	    i++;
	}
    }

    protected static String vocaliseText(String text) throws Exception {
	//return vocaliseTextOld(text);
	return vocaliseTextMishkal(text);
    }



    protected static String vocaliseTextOld(String text) throws Exception {

	String url = "http://localhost:8080/vocalise?text=";
	url+=URLEncoder.encode(text, "UTF-8");
	System.out.println("Vocalise url: "+url);

	InputStream is = new URL(url).openStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) 
            response.append(inputLine);

        in.close();

        String vocalised = response.toString();
	
	System.out.println("Vocalised text: "+vocalised);
	
	return vocalised;

    }

    protected static String vocaliseTextMishkal(String text) throws Exception {

	String url = "http://localhost:8080/ajaxGet?action=Tashkeel2&text=";
	url+=URLEncoder.encode(text, "UTF-8");
	System.out.println("Vocalise url: "+url);

	InputStream is = new URL(url).openStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) 
            response.append(inputLine);

        in.close();

        String vocalised = response.toString();	
	System.out.println("Vocalised text: "+vocalised);

	JsonReader reader = Json.createReader(new StringReader(vocalised));
        JsonObject vocObject = reader.readObject();         
        reader.close();

	List<String> vocList = new ArrayList<String>();
	JsonArray resultsArray = vocObject.getJsonArray("result");

	for (int i = 0; i < resultsArray.size(); ++i) {
	    JsonObject result = resultsArray.getJsonObject(i);

	    //"chosen" appears to be with case endings, "semi" without
	    //Maybe better to use "chosen" and skip final diacritic? 
	    //String chosen = result.getString("chosen");
	    String chosen = result.getString("semi");

	    if ( chosen.equals("") ) {
		chosen = result.getString("chosen");
	    }

	    vocList.add(chosen);
	}


	// for (JsonValue jsonValue : resultsArray) {        
        //     System.out.println(jsonValue.toString());
	//     String chosen = ((JsonObject) resultObj).getString("chosen");
	//     //String chosen = jsonValue.getJsonObject("chosen");
	//     vocList.add(chosen);
        // }

	
	//String[] vocArray = new String[vocList.size()];
	//vocList.toArray(vocArray);
	//return vocArray;

	String vocString = "";

	for (String s : vocList)
	    {
		vocString += s + " ";
	    }

	return vocString.trim();
    }


    
    protected void checkForNumbers(Document doc) {
	TreeWalker tw = ((DocumentTraversal) doc).createTreeWalker(doc, NodeFilter.SHOW_ELEMENT, new NameNodeFilter(MaryXML.TOKEN), false);
	Element t = null;
	while ((t = (Element) tw.nextNode()) != null) {
	    if (MaryDomUtils.hasAncestor(t, MaryXML.SAYAS) || t.hasAttribute("ph") || t.hasAttribute("sounds_like")) {
		// ignore token
		continue;
	    }
	    String origText = MaryDomUtils.tokenText(t);
	    System.err.println("Looking for number: "+origText);

	    if (origText.matches("[0-9]+([,.][0-9]+)*")) {
		System.err.println("FOUND NUMBER: "+origText);

		//Decimal point is ok, but comma is not (1,000,000 -> 1000000)
		String cleanedText = origText.replace(",", ""); 

		String expanded = expandNumber(Double.parseDouble(cleanedText));
		//System.err.println("Setting token text to "+expanded);
		//MaryDomUtils.setTokenText(t, expanded);
		List mtu = makeNewTokens(doc, expanded, true, origText, false);
		System.err.println("MTU: "+mtu);
		ArrayList<Element> oldtokens = new ArrayList<Element>();
		oldtokens.add(t);
		replaceTokens(oldtokens, mtu);

		//tw is now in the wrong place, need to correct
		Element lastToken = getLastToken(mtu);
		assert lastToken != null;
		tw.setCurrentNode(lastToken);
		System.err.println("set treewalker position:" + MaryDomUtils.getPlainTextBelow((Element) tw.getCurrentNode()));


		
	    }
	    // if token isn't ignored but there is no handling rule don't add MTU
	    //if (!origText.equals(MaryDomUtils.tokenText(t))) {
	    //	MaryDomUtils.encloseWithMTU(t, origText, null);
	    //}
	}
    }

    protected String expandNumber(double number) {
	    this.rbnf.setDefaultRuleSet(cardinalRule);
	    String expanded = this.rbnf.format(number); 
	    logger.debug("Expanding cardinal "+number+" using rule "+cardinalRule+" -> "+expanded);
	    System.err.println("Expanding cardinal "+number+" using rule "+cardinalRule+" -> "+expanded);
	    return expanded;
	}

	// protected String expandOrdinal(double number) {
	//     logger.info("Expanding ordinal "+number+" using rule "+ordinalRule);
	//     this.rbnf.setDefaultRuleSet(ordinalRule);
	//     String expanded = this.rbnf.format(number); 
	//     logger.debug("Expanding ordinal "+number+" using rule "+ordinalRule+" -> "+expanded);
	//     return expanded;
	// }

	// protected String expandOrdinal_e(String rule, double number) {
	//     logger.info("Expanding ordinal "+number+" using rule "+rule);
	//     this.rbnf.setDefaultRuleSet(rule);
	//     String expanded = this.rbnf.format(number); 
	//     logger.debug("Expanding ordinal "+number+" using rule "+rule+" -> "+expanded);
	//     return expanded;
	// }

	/**
	 * Try to extract the rule name for "expand ordinal" from the given RuleBasedNumberFormat.
	 * <p/>
	 * The rule name is locale sensitive, but usually starts with "%spellout-ordinal".
	 *
	 * @param rbnf
	 *            The RuleBasedNumberFormat from where we will try to extract the rule name.
	 * @return The rule name for "ordinal spell out".
	 */
	protected String getOrdinalRuleName(final RuleBasedNumberFormat rbnf) {
		List<String> l = Arrays.asList(rbnf.getRuleSetNames());
		System.err.println("RNBF list for 'ar':"+l);
		//[%spellout-cardinal-masculine, %spellout-cardinal-feminine, %spellout-numbering, %spellout-numbering-year]
		//But http://unicode.org/repos/cldr/trunk/common/rbnf/ar.xml has spellout-ordinal-masculine and spellout-ordinal-feminine
		if (l.contains("%spellout-ordinal")) {
			return "%spellout-ordinal";
		} else {
			for (String string : l) {
				if (string.startsWith("%spellout-ordinal")) {
					return string;
				}
			}
		}
		throw new UnsupportedOperationException("The locale " + rbnf.getLocale(ULocale.ACTUAL_LOCALE)
				+ " doesn't support ordinal spelling.");
	}



    /*
      makeNewTokens and replaceTokens from marytts-lang-de ExpansionPattern
      getLastToken from marytts-lang-de Preprocess
     */

    protected List<Element> makeNewTokens(Document doc, String newText, boolean createMtu, String origText, boolean forceAccents) {
		if (newText == null || newText.length() == 0) {
			// unusable input
			return null; // failure
		}
		Pattern rePron = Pattern.compile("\\[(.*)\\]"); // pronunciation in square brackets
		StringTokenizer st = new StringTokenizer(newText);
		ArrayList<Element> newTokens = new ArrayList<Element>();
		while (st.hasMoreTokens()) {
			// Create new token element:
			String text = st.nextToken();
			Element newT = MaryXML.createElement(doc, MaryXML.TOKEN);
			Matcher remPron = rePron.matcher(text);
			if (remPron.find()) {
				String pron = remPron.group(1); // would be $1 in perl
				text = rePron.matcher(text).replaceFirst(""); // delete pronunciation from word
				newT.setAttribute("ph", pron);
			}
			MaryDomUtils.setTokenText(newT, text);
			System.err.println("makeNewTokens creating token "+text);
			if (forceAccents)
				newT.setAttribute("accent", "unknown");
			newTokens.add(newT);
		}
		if (createMtu) {
			// create mtu element enclosing the expanded tokens:
			Element mtu = MaryXML.createElement(doc, MaryXML.MTU);
			mtu.setAttribute("orig", origText);
			mtu.setAttribute("accent", "last");
			for (Iterator<Element> it = newTokens.iterator(); it.hasNext();) {
			    Element e = it.next();
			    System.err.println("makeNewTokens adding token to mtu: "+MaryDomUtils.tokenText(e));
			    mtu.appendChild((Element) e);
			}
			List<Element> result = new ArrayList<Element>();
			result.add(mtu);
			System.err.println("makeNewTokens returning mtu: "+mtu);
			return result;
		} else {
			return newTokens;
		}
	}

	protected void replaceTokens(List<Element> oldTokens, List<Element> newTokens) {
		if (oldTokens == null || oldTokens.isEmpty() || newTokens == null || newTokens.isEmpty()) {
			// unusable input
			throw new NullPointerException("Have received null or empty argument.");
		}
		Element oldT = null;
		Iterator<Element> itOld = oldTokens.iterator();
		Iterator<Element> itNew = newTokens.iterator();
		while (itNew.hasNext()) {
			Element newT = (Element) itNew.next();
			// Retrieve old token element:
			if (itOld.hasNext()) // this is true at least once
				oldT = (Element) itOld.next();
			oldT.getParentNode().insertBefore(newT, oldT);
			if (itOld.hasNext()) // only remove this old t if there is another one
				oldT.getParentNode().removeChild(oldT);
		}
		if (!itOld.hasNext()) { // only need to remove oldT
			oldT.getParentNode().removeChild(oldT);
		} else {
			// there were more old than new tokens
			while (itOld.hasNext()) {
				oldT = (Element) itOld.next();
				oldT.getParentNode().removeChild(oldT);
			}
		}
		// Now go through the new tokens again and see if there are any
		// useless mtu combinations. If so, the "inner" one wins.
		itNew = newTokens.iterator();
		while (itNew.hasNext()) {
			Element mtu = (Element) itNew.next();
			if (!mtu.getTagName().equals(MaryXML.MTU))
				continue;
			Element parent = (Element) mtu.getParentNode();
			if (!parent.getTagName().equals(MaryXML.MTU))
				continue;
			// OK, got an mtu inside an mtu
			if (MaryDomUtils.getPreviousSiblingElement(mtu) != null || MaryDomUtils.getNextSiblingElement(mtu) != null)
				continue;
			if (!parent.getAttribute("orig").equals(mtu.getAttribute("orig")))
				continue;
			// OK, mtu and parent are mtu tags, there is no other element in parent
			// than mtu, and both have the same orig value
			// => delete parent
			Element grandParent = (Element) parent.getParentNode();
			grandParent.insertBefore(mtu, parent);
			grandParent.removeChild(parent);
		}
	}


	private Element getLastToken(List<Element> l) {
		if (l == null)
			throw new NullPointerException("Received null argument");
		if (l.isEmpty())
			throw new IllegalArgumentException("Received empty list");
		for (int i = l.size() - 1; i >= 0; i--) {
			Element e = (Element) l.get(i);
			Element t = null;
			if (e.getTagName().equals(MaryXML.TOKEN)) {
				t = e;
			} else {
				t = MaryDomUtils.getLastElementByTagName(e, MaryXML.TOKEN);
			}
			if (t != null)
				return t;
		}
		return null;
	}

}
