/**
 * Copyright 2011 DFKI GmbH.
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

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;



import marytts.exceptions.MaryConfigurationException;
import marytts.util.MaryUtils;

public class JPhonemiser extends marytts.modules.JPhonemiser {

    public JPhonemiser() throws IOException, MaryConfigurationException {
        super("ar.");
    }
    
    @Override
    public String phonemise(String text, String pos, StringBuilder g2pMethod)
    {
        // First, try a simple userdict and lexicon lookup:

	System.err.println("Text: "+text);


        text = text.replaceAll("[0-9]+","");
        String result = userdictLookup(text, pos);
        if (result != null) {
            g2pMethod.append("userdict");
            return result;
        }
        
        result = lexiconLookup(text, pos);
        if (result != null) {
            g2pMethod.append("lexicon");
            return result;
        }

        // Lookup attempts failed. Try normalising exotic letters
        // (diacritics on vowels, etc.), look up again:
        String normalised = MaryUtils.normaliseUnicodeLetters(text, getLocale());
        if (!normalised.equals(text)) {
            result = userdictLookup(normalised, pos);
            if (result != null) {
                g2pMethod.append("userdict");
                return result;
            }
            result = lexiconLookup(normalised, pos);
            if (result != null) {
                g2pMethod.append("lexicon");
                return result;
            }
        }
           
        // Cannot find it in the lexicon -- apply letter-to-sound rules
        // to the normalised form
        

	//HB It would probably be better to not use lts rules, but a simpler conversion of buckwalter to phonetics.
	//For now just correct the obvious error that a final "p" (ta marbuta) is pronounced /t/

	//just remove it
	//text = text.replaceAll("p( |$)","\1");
        //String phones = lts.predictPronunciation(text);

	//Trying instead a very simple buckwalter to phonetic mapping
	String phones = buckwalterToPhonetic(text);
	System.out.println("Text: "+text+", phones: "+phones);

        result = lts.syllabify(phones);
	System.out.println("Syllabified: "+result);


        if (result != null) {
            g2pMethod.append("bw2p rules");
            return result;
        }

        return null;
    }

    private String buckwalterToPhonetic(String text) {
	String phn = "";
	String rest = text.trim();

	//AbtvjHxd*rzs$SDTZEgfqklmnhwy1IOW}1{2YauiFNK_op_

	HashMap<String, String> hmap = new HashMap<String, String>();
	hmap.put("A","<");
	hmap.put("b","b");
	hmap.put("t","t");
	hmap.put("v","v");
	hmap.put("j","j");
	hmap.put("H","H");
	hmap.put("x","x");
	hmap.put("d","d");
	hmap.put("*","*");
	hmap.put("r","r");
	hmap.put("z","z");
	hmap.put("s","s");
	hmap.put("$","$");
	hmap.put("S","S");
	hmap.put("D","D");
	hmap.put("T","T");
	hmap.put("Z","Z");
	hmap.put("E","<");

	hmap.put("g","g");
	hmap.put("f","f");
	hmap.put("q","q");
	hmap.put("k","k");
	hmap.put("l","l");
	hmap.put("m","m");
	hmap.put("n","n");
	hmap.put("h","h");
	hmap.put("w","w");
	hmap.put("y","y");
	hmap.put("1","<");
	hmap.put("I","<");
	hmap.put("O","<");
	hmap.put("W","<");
	//??hmap.put("}","<");
	//??hmap.put("{","<");
	//??hmap.put("2","<");
	hmap.put("Y","a"); //??

	hmap.put("a","a");
	hmap.put("u","u");
	hmap.put("i","i");

	hmap.put("F","");
	hmap.put("N","");
	hmap.put("K","");

	hmap.put("_","");
	hmap.put("o","");



	hmap.put("iy_","ii0");
	hmap.put("p","h");

	while (!rest.equals("")) {
	    //Earabiy_ap

	    Set set = hmap.entrySet();
	    Iterator iterator = set.iterator();
	    while(iterator.hasNext()) {
		Map.Entry mentry = (Map.Entry)iterator.next();
		if ( rest.startsWith((String)mentry.getKey()) ) {
		    rest = rest.replaceFirst((String)mentry.getKey(),"");
		    phn += " "+mentry.getValue()+" ";

		}
	    }
	    System.out.println("phn: "+phn+", rest: \""+rest+"\"");
			    
	}
	return phn;
    }




}
