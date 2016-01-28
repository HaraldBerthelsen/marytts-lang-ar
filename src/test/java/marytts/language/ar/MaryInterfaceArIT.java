package marytts.language.ar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.datatypes.MaryDataType;
import marytts.util.dom.DomUtils;
import marytts.datatypes.MaryXML;

import org.junit.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import marytts.util.dom.NameNodeFilter;

import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;



public class MaryInterfaceArIT {

    public String getStringFromDoc(Document doc)    {
	DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
	LSSerializer lsSerializer = domImplementation.createLSSerializer();
	return lsSerializer.writeToString(doc);   
    }

    @Test
    public void canSetLocale() throws Exception {
	MaryInterface mary = new LocalMaryInterface();
	Locale loc = new Locale("ar");
	
	assertTrue(!loc.equals(mary.getLocale()));
	mary.setLocale(loc);
	assertEquals(loc, mary.getLocale());
    }
    

    //THIS WILL ONLY WORK IF THE MISHKAL WEBSERVER IS RUNNING
    @Test
    public void canProcessArabicToTokens() throws Exception {
    	// setup
    	MaryInterface mary = new LocalMaryInterface();
    	mary.setInputType(MaryDataType.TEXT.name());
    	mary.setOutputType(MaryDataType.TOKENS.name());
    	mary.setLocale(new Locale("ar"));

    	String example = "إلى المملكة المغربية";


    	// exercise
    	Document allos = mary.generateXML(example);
    	// verify
    	assertNotNull(allos);

    }

    @Test
    public void canProcessPunctuatedArabicToTokens() throws Exception {
    	// setup
    	MaryInterface mary = new LocalMaryInterface();
    	mary.setInputType(MaryDataType.TEXT.name());
    	mary.setOutputType(MaryDataType.TOKENS.name());
    	mary.setLocale(new Locale("ar"));

    	String example = "مدينة شِبام الأثريَّة التاريخيَّة، إحدى أقدم مُدن";


    	// exercise
    	Document doc = mary.generateXML(example);
    	// verify
    	assertNotNull(doc);

	//We should get 8 tokens from this example, 7 words and one comma
	TreeWalker tw = ((DocumentTraversal) doc).createTreeWalker(doc, NodeFilter.SHOW_ELEMENT,
								   new NameNodeFilter(MaryXML.TOKEN), false);
	Element t = null;
	int count = 0;
	while ((t = (Element) tw.nextNode()) != null) {
	    count++;
	}
	assertEquals(8, count);


    }

    @Test
    public void canProcessVocalisedArabicToTokens() throws Exception {
    	// setup
    	MaryInterface mary = new LocalMaryInterface();
    	mary.setInputType(MaryDataType.TEXT.name());
    	mary.setOutputType(MaryDataType.TOKENS.name());
    	mary.setLocale(new Locale("ar"));

    	String example = MaryDataType.getExampleText(MaryDataType.TEXT, mary.getLocale());


    	// exercise
    	Document allos = mary.generateXML(example);
    	// verify
    	assertNotNull(allos);

    }

    // @Test
    // public void canProcessBuckwalterToTokens() throws Exception {
    // 	// setup
    // 	MaryInterface mary = new LocalMaryInterface();
    // 	mary.setInputType(MaryDataType.TEXT.name());
    // 	mary.setOutputType(MaryDataType.TOKENS.name());
    // 	mary.setLocale(new Locale("ar"));

    // 	String example = "IilaY Alomamolakap Alomugorabiy_ap";

    // 	// exercise
    // 	Document allos = mary.generateXML(example);
    // 	// verify
    // 	assertNotNull(allos);

    // }
    
    @Test
    public void canProcessTokensToAllophones() throws Exception {
    	// setup
    	MaryInterface mary = new LocalMaryInterface();
    	mary.setInputType(MaryDataType.TOKENS.name());
    	mary.setOutputType(MaryDataType.ALLOPHONES.name());
    	mary.setLocale(new Locale("ar"));
    	String example = MaryDataType.getExampleText(MaryDataType.TOKENS, mary.getLocale());
    	System.err.println("Arabic example text: "+example);
    	assertNotNull(example);
    	Document tokens = DomUtils.parseDocument(example);
    	// exercise
    	Document allos = mary.generateXML(tokens);
    	// verify
    	assertNotNull(allos);
    }
    
    // @Test
    // public void canProcessTokensToAcoustparams() throws Exception {
    // 	// setup
    // 	MaryInterface mary = new LocalMaryInterface();
    // 	mary.setInputType(MaryDataType.TOKENS.name());
    // 	mary.setOutputType(MaryDataType.ACOUSTPARAMS.name());
    // 	mary.setLocale(new Locale("ar"));
    // 	String example = MaryDataType.getExampleText(MaryDataType.TOKENS, mary.getLocale());
    // 	System.err.println("Arabic example text: "+example);
    // 	assertNotNull(example);
    // 	Document tokens = DomUtils.parseDocument(example);
    // 	// exercise
    // 	Document acparams = mary.generateXML(tokens);
    // 	// verify
    // 	assertNotNull(acparams);

    // 	System.err.println("Output data type: "+mary.getOutputType());
    // 	//HB this is not right, PHONEMES get printed here, how does that happen? Where are the acoustparams, and why no complaint?	
    // 	System.err.println("Result: "+getStringFromDoc(acparams));
    // }
}
