/**
 * Copyright 2000-2006 DFKI GmbH.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * This file is part of MARY TTS.
 *
 * MARY TTS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package marytts.language.ar;

import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.datatypes.MaryXML;
import marytts.server.MaryProperties;
import marytts.util.MaryUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.*;
import java.net.*;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
/**
 * Embed plain text input into a raw (untokenised) MaryXML document.
 * 
 * @author Marc Schr&ouml;der
 */

public class TextToMaryXML extends marytts.modules.InternalModule {
    private DocumentBuilderFactory factory = null;
    private DocumentBuilder docBuilder = null;
    private boolean splitIntoParagraphs;

    public TextToMaryXML() {
	super("TextToMaryXML", MaryDataType.TEXT, MaryDataType.RAWMARYXML, new Locale("ar"));
	splitIntoParagraphs = MaryProperties.getBoolean("texttomaryxml.splitintoparagraphs");
    }

    public void startup() throws Exception {
	if (factory == null) {
	    factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(true);
	}
	if (docBuilder == null) {
	    docBuilder = factory.newDocumentBuilder();
	}
	super.startup();
    }

    public MaryData process(MaryData d) throws Exception {
	String plainText = MaryUtils.normaliseUnicodePunctuation(d.getPlainText());
	MaryData result = new MaryData(outputType(), d.getLocale(), true);
	Document doc = result.getDocument();
	Element root = doc.getDocumentElement();
	Locale l = determineLocale(plainText, d.getLocale());
	root.setAttribute("xml:lang", MaryUtils.locale2xmllang(l));
	if (splitIntoParagraphs) { // Empty lines separate paragraphs
	    String[] inputTexts = plainText.split("\\n(\\s*\\n)+");
	    for (int i = 0; i < inputTexts.length; i++) {
		String paragraph = inputTexts[i].trim();
		if (paragraph.length() == 0)
		    continue;
		appendParagraph(paragraph, root, d.getLocale());
	    }
	} else { // The whole text as one single paragraph
	    appendParagraph(plainText, root, d.getLocale());
	}
	result.setDocument(doc);
	return result;
    }

    /**
     * Append one paragraph of text to the rawmaryxml document. If the text language (as determined by #getLanguage(text)) differs
     * from the enclosing document's language, the paragraph element is enclosed with a <code>&lt;voice xml:lang="..."&gt;</code>
     * element.
     * 
     * @param text
     *            the paragraph text.
     * @param root
     *            the root node of the rawmaryxml document, where to insert the paragraph.
     * @param defaultLocale
     *            the default locale, in case the language of the text cannot be determined.
     */
    private void appendParagraph(String text, Element root, Locale defaultLocale) throws Exception {
	Element insertHere = root;
	String rootLanguage = root.getAttribute("xml:lang");
	String textLanguage = MaryUtils.locale2xmllang(determineLocale(text, defaultLocale));
	if (!textLanguage.equals(rootLanguage)) {
	    Element voiceElement = MaryXML.appendChildElement(root, MaryXML.VOICE);
	    voiceElement.setAttribute("xml:lang", textLanguage);
	    insertHere = voiceElement;
	}

	// HB 151109 If the text is in Arabic script, change it to buckwalter
	System.out.println("Input text on next line:");
	System.out.println(text);

	if ( containsArabic(text) ) {

	    if ( !isVocalised(text) ) {
		text = vocaliseText(text);
	    }

	    text = arabicToBuckwalter(text);
	}

	System.out.println("Buckwalter text on next line:");
	System.out.println(text);

	insertHere = MaryXML.appendChildElement(insertHere, MaryXML.PARAGRAPH);
	// Now insert the entire plain text as a single text node
	insertHere.appendChild(root.getOwnerDocument().createTextNode(text));
	// And, for debugging, read it:
	Text textNode = (Text) insertHere.getFirstChild();
	String textNodeString = textNode.getData();
	logger.debug("textNodeString=`" + textNodeString + "'");
    }

    /**
     * Try to determine the locale of the given text. This implementation simply returns the default locale; subclasses can try to
     * do something fancy here.
     * 
     * @param text
     *            the text whose locale to determine
     * @param defaultLocale
     *            the default locale of the document.
     * @return the locale as inferred from the text and the default locale
     */
    protected Locale determineLocale(String text, Locale defaultLocale) {
	if (defaultLocale == null) {
	    defaultLocale = Locale.getDefault();
	    logger.warn("Locale is null, overriding with " + defaultLocale);
	}
	return defaultLocale;
    }


    private static boolean containsArabic(String text) {
	for (char ch: text.toCharArray()) {
	    if ( isArabic(ch) ) {
		return true;
	    }
	}
	return false;	    
    }



    private static boolean isArabic(char c) {
	final String arabic = "ابتثجحخدذرزسشصضطظعغفقكلمنهويءإأؤئآٱٰىًٌٍَُِّْةـ";
	return arabic.indexOf(c) >= 0;
    }

    private static boolean isVocalised(String text) {
	for (char ch: text.toCharArray()) {
	    if ( isDiacritic(ch) ) {
		return true;
	    }
	}
	return false;	    
    }

    private static boolean isDiacritic(char c) {
	final String diacritics = "\u0652\u064e\u064f\u0650\u064b\u064c\u064d\u0651";
	int firstDiacriticIndex = diacritics.indexOf(c);
	if (firstDiacriticIndex >= 0) {
	    System.out.println("Found diacritic "+c+" at index "+firstDiacriticIndex);
	    return true;
	}
	return false;
    }


    private static String vocaliseTextOLD(String text) throws Exception {

	String url = "http://localhost:8080/ajaxGet?action=TashkeelText&text=";
	url+=URLEncoder.encode(text);
	System.out.println("Vocalise url: "+url);
	InputStream is = new URL(url).openStream();
	JsonReader rdr = Json.createReader(is);
	JsonObject obj = rdr.readObject();
	String vocalised = obj.getString("result");
	
	System.out.println("Vocalised text: "+vocalised);
	
	return vocalised;

    }

    private static String vocaliseText(String text) throws Exception {

	String url = "http://localhost:8080/vocalise?text=";
	url+=URLEncoder.encode(text, "UTF-8");
	System.out.println("Vocalise url: "+url);

	InputStream is = new URL(url).openStream();

	//JsonReader rdr = Json.createReader(is);
	//JsonObject obj = rdr.readObject();
	//String vocalised = obj.getString("result");

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



    private static String arabicToBuckwalter(String text) {
	String ar = "";
	for (char ch: text.toCharArray()) {
	    ar += arabicToBuckwalter(ch);
	}
	return ar;
    }


    private static char arabicToBuckwalter(char c) {
	final String arabic = "ابتثجحخدذرزسشصضطظعغفقكلمنهويءإأؤئآٱٰىًٌٍَُِّْةـ";
	//final String buckwalter = "AbtvjHxd*rzs$SDTZEgfqklmnhwy'IOW}|{`YauiFNK_op_";
	//' -> 1 WRONG it's lone hamza TODO replace with something else in corpus (or is it ok to keep '?)
	//| -> 1
	//` -> 2
	final String buckwalter = "AbtvjHxd*rzs$SDTZEgfqklmnhwy1IOW}1{2YauiFNK_op_";
	int index = arabic.indexOf(c);
	if (index >= 0)
	    return buckwalter.charAt(index);
	return c; //what is the right thing to do ?? maybe check for space, punctuation, etc?
    }


}
