package marytts.language.ar;

import marytts.language.ar.JPhonemiser;
import marytts.util.dom.DomUtils;

import org.custommonkey.xmlunit.*;
import org.testng.Assert;
import org.testng.annotations.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author HB
 *
 *
 */
public class JPhonemiserTest {

	private static JPhonemiser module;

	@BeforeSuite
	public static void setUpBeforeClass() throws Exception {
		module = new JPhonemiser();
	}

	@DataProvider(name = "PhonetiseData")
	private Object[][] phonetiseData() {
		// @formatter:off
	    //These tests are not correct - but anyway this is a place to add tests later!
	    return new Object[][] { { "وَاحِد", "' w a - H i1 d"}, 
				    { "إِثْنانِ", "' < i0 - ^ n aa - n i0" } };
		// @formatter:on
	}


	@Test(dataProvider = "PhonetiseData")
	public void testPhonetise(String test, String trans) throws Exception {
	    String actual = module.phonemise(test).trim();
	    Assert.assertEquals(actual, trans);
	}


}
