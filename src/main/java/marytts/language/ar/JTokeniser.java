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

import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.datatypes.MaryXML;
import marytts.util.dom.MaryDomUtils;
import marytts.util.dom.NameNodeFilter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

/**
 * 
 * @author Marc Schr&ouml;der
 */
public class JTokeniser extends marytts.modules.JTokeniser {

	/**
     * 
     */
	public JTokeniser() {
		super(MaryDataType.RAWMARYXML, MaryDataType.TOKENS, new Locale("ar"));
		//needs to setTokenizerLanguage("en") otherwise it won't work..
		//But that's really bad - we would want tokenisation with arabic punctuation etc
		setTokenizerLanguage("en");
	}

	public MaryData process(MaryData d) throws Exception {
	    filterPunctuation(d);
	    MaryData result = super.process(d);
	    return result;
	}

	protected void filterPunctuation(MaryData d) {
	    Document doc = d.getDocument();
	    NodeIterator ni = ((DocumentTraversal) doc).createNodeIterator(doc, NodeFilter.SHOW_ELEMENT, new NameNodeFilter(
															    MaryXML.PARAGRAPH), false);
	    Element p = null;
	    while ((p = (Element) ni.nextNode()) != null) {
		Node textNode = p.getFirstChild();
		String s = textNode.getNodeValue();

		System.err.println("FilterPunctuation");
		System.err.println(s);

		s = s.replaceAll("،", ",");
		//s = s.replaceAll("XX", "YY");

		System.err.println(s);

		textNode.setNodeValue(s);
	    }
	}


}
