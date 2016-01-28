package marytts.language.ar;

import marytts.language.ar.JTokeniser;
import marytts.util.dom.DomUtils;

import org.custommonkey.xmlunit.*;
import org.testng.Assert;
import org.testng.annotations.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.util.Locale;
import marytts.datatypes.MaryData;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.datatypes.MaryDataType;
import marytts.datatypes.MaryXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import marytts.util.dom.NameNodeFilter;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author HB
 *
 *
 */
public class JTokeniserTest {

	private static JTokeniser module;

	@BeforeSuite
	public static void setUpBeforeClass() throws Exception {
		module = new JTokeniser();
	}

	// @DataProvider(name = "TokeniseData")
	// private Object[][] tokeniseiseData() {
	// 	// @formatter:off
	//     //These tests look weird in emacs - it is just {<String>, <int>}
	//     return new Object[][] { { "وَاحِد", 1}, 
	// 			    { "إِثْنانِ", 1 } };
	// 	// @formatter:on
	// }


	// @Test(dataProvider = "TokeniseData")
	// public void testTokenise(String test, int expected) throws Exception {

	//     MaryInterface mary = new LocalMaryInterface();
	//     mary.setInputType(MaryDataType.TEXT.name());
	//     mary.setOutputType(MaryDataType.RAWMARYXML.name());
	//     mary.setLocale(new Locale("ar"));
	    
	//     // exercise
	//     Document rawDoc = mary.generateXML(test.trim());
	//     // verify
	//     assertNotNull(rawDoc);

	//     //Document doc = module.process(rawDoc);
	//     MaryData doc = module.process(rawDoc);

	//     //We should get 8 tokens from this example, 7 words and one comma
	//     TreeWalker tw = ((DocumentTraversal) doc).createTreeWalker(doc, NodeFilter.SHOW_ELEMENT,
	// 							       new NameNodeFilter(MaryXML.TOKEN), false);
	//     Element t = null;
	//     int count = 0;
	//     while ((t = (Element) tw.nextNode()) != null) {
	// 	count++;
	//     }
	//     assertEquals(expected, count);
	    

	// }


}
