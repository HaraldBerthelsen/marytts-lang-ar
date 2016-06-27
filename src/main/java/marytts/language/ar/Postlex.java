/**
 * Copyright 2002 DFKI GmbH.
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

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.datatypes.MaryXML;
import marytts.modules.InternalModule;
import marytts.modules.PronunciationModel;
import marytts.modules.phonemiser.AllophoneSet;
import marytts.util.dom.MaryDomUtils;
import marytts.util.dom.NameNodeFilter;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

/**
 * The postlexical phonological processes module. Only for removing initial A..
 * 
 * @author Harald Berthelsen
 */

public class Postlex extends PronunciationModel {

    public Postlex() {
	super(new Locale("ar"));
	System.out.println("Arabic Postlex LOADING");
    }
    
    public MaryData process(MaryData d) throws Exception {
	System.out.println("Arabic Postlex START");
	Document doc = d.getDocument();
	dropInitialAlef(doc);
	return super.process(d);
    }
    
    //Not exactly clever but seems to do the job.
    //But drops stress..
    private void dropInitialAlef(Document doc) throws DOMException {
	TreeWalker tw = ((DocumentTraversal) doc).createTreeWalker(doc, NodeFilter.SHOW_ELEMENT, new NameNodeFilter(MaryXML.TOKEN),
								   false);
	Element m = null;
	boolean prevEndsWithVowel = false;
	while ((m = (Element) tw.nextNode()) != null) {
	    System.out.println("Element: "+m.getTagName());
	    if (m != null && m.getTagName().equals(MaryXML.TOKEN)) {
		String transcription = m.getAttribute("ph");
		System.out.println("transcription: "+transcription+", prevEndsWithVowel: "+prevEndsWithVowel);
		if ( transcription.startsWith("' a ") && prevEndsWithVowel ) {
		    System.out.println("Removing A");
		    m.setAttribute("ph", transcription.replaceAll("^' a -?","' "));
		}
		if ( transcription.matches(".*[AUIaui][01]?$") ) {
		    prevEndsWithVowel = true;
		}
		
	    }
	}
    }
    
}
