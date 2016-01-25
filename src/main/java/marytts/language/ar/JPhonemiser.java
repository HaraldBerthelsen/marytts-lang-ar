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
    
    public String phonemise(String text) {
	String pos = "NONE";
	StringBuilder g2pMethod = new StringBuilder();
	return phonemise(text, pos, g2pMethod);
    }

    @Override
    public String phonemise(String text, String pos, StringBuilder g2pMethod)
    {

	//Arabic question:
	//Where to remove initial A if previous word ends with vowel?
	//Here only one word at a time..
	//It is now done in Postlex.java

	text = arabicToBuckwalter(text);


        // First, try a simple userdict and lexicon lookup:

	//System.err.println("Text: "+text);


        String result = userdictLookup(text, pos);
        if (result != null) {
            g2pMethod.append("userdict");
            return result;
        }
        
	//HB 151118 Don't do lexicon lookup until the lexicon is fixed!
        //result = lexiconLookup(text, pos);
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
	    //HB 151118 Don't do lexicon lookup until the lexicon is fixed!
            //result = lexiconLookup(normalised, pos);
            if (result != null) {
                g2pMethod.append("lexicon");
                return result;
            }
        }


           
        // Cannot find it in the lexicon -- apply letter-to-sound rules
        // to the normalised form
        

	//HB It is probably better to not use lts rules, but a simpler conversion of buckwalter to phonetics.
        //String phones = lts.predictPronunciation(text);

	//Trying instead a simple buckwalter to phonetic mapping (should be the same as in Arabic-Phonetiser)

	/*
	  //This check should be done before vocalisation - when the word appears here it's been vocalised and doesn't match the fixed words (ex lndn appears as lanodan)
	result = isFixedWord(text);
	if (result != null) {
	    g2pMethod.append("lexicon");
	    return result;
	}
	*/

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

    private String isFixedWord(String word) {

	HashMap<String, String[]> fixedWords = new HashMap<String, String[]>();
	fixedWords.put("h*A", new String[] { "h aa * aa", "h aa * a" } );
	fixedWords.put("h*h", new String[] { "h aa * i0 h i0", "h aa * i1 h" } );
	fixedWords.put("h*An", new String[] { "h aa * aa n i0", "h aa * aa n" } );
	fixedWords.put("hWlA\'", new String[] { "h aa < u0 l aa < i0", "h aa < u0 l aa <" } );
	fixedWords.put("*lk", new String[] { "* aa l i0 k a", "* aa l i0 k" } );
	fixedWords.put("k*lk", new String[] { "k a * aa l i0 k a", "k a * aa l i1 k" } );
	fixedWords.put("*lkm", new String[] { "* aa l i0 k u1 m" } );
	fixedWords.put("Owl}k", new String[] { "< u0 l aa < i0 k a", "< u0 l aa < i1 k" } );
	fixedWords.put("Th", new String[] { "T aa h a" } );
	fixedWords.put("lkn", new String[] { "l aa k i0 nn a", "l aa k i1 n" } );
	fixedWords.put("lknh", new String[] { "l aa k i0 nn a h u0" } );
	fixedWords.put("lknhm", new String[] { "l aa k i0 nn a h u1 m" } );
	fixedWords.put("lknk", new String[] { "l aa k i0 nn a k a", "l aa k i0 nn a k i0" } );
	fixedWords.put("lknkm", new String[] { "l aa k i0 nn a k u1 m" } );
	fixedWords.put("lknkmA", new String[] { "l aa k i0 nn a k u0 m aa" } );
	fixedWords.put("lknnA", new String[] { "l aa k i0 nn a n aa" } );
	fixedWords.put("AlrHmn", new String[] { "rr a H m aa n i0", "rr a H m aa n" } );
	fixedWords.put("AllA", new String[] { "ll aa h i0", "ll aa h", "ll AA h u0", "ll AA h a", "ll AA h", "ll A" } );
	fixedWords.put("h*yn", new String[] { "h aa * a y n i0", "h aa * a y n" } );
	
	fixedWords.put("nt", new String[] { "n i1 t" } );
	fixedWords.put("fydyw", new String[] { "v i0 d y uu1" } );
	fixedWords.put("lndn", new String[] { "l A n d u1 n" } );

	String result;
	if ( fixedWords.containsKey(word) ) {
	    result = fixedWords.get(word)[0];
	} else {
	    result = "";
	}
	
	return result;
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
	if (index >= 0) {
	    char bw = buckwalter.charAt(index);
	    System.err.println("ar2bw: "+c+" -> "+bw);
	    return bw;
	}
	return c; //what is the right thing to do ?? maybe check for space, punctuation, etc?
    }


    private String buckwalterToPhonetic(String text) {

	//AbtvjHxd*rzs$SDTZEgfqklmnhwy1IOW}1{2YauiFNK_op_

	HashMap<String, String> unambiguousConsonantMap = new HashMap<String, String>();
	unambiguousConsonantMap.put("b","b");
	unambiguousConsonantMap.put("t","t");
	unambiguousConsonantMap.put("v","^"); 
	unambiguousConsonantMap.put("j","j");
	unambiguousConsonantMap.put("H","H");
	unambiguousConsonantMap.put("x","x");
	unambiguousConsonantMap.put("d","d");
	unambiguousConsonantMap.put("*","*"); //check is * or 2 used in "my" buckwalter
	unambiguousConsonantMap.put("2","*");
	unambiguousConsonantMap.put("r","r");
	unambiguousConsonantMap.put("z","z");
	unambiguousConsonantMap.put("s","s");
	unambiguousConsonantMap.put("$","$");
	unambiguousConsonantMap.put("S","S");
	unambiguousConsonantMap.put("D","D");
	unambiguousConsonantMap.put("T","T");
	unambiguousConsonantMap.put("Z","Z");
	unambiguousConsonantMap.put("E","E");

	unambiguousConsonantMap.put("g","g");
	unambiguousConsonantMap.put("f","f");
	unambiguousConsonantMap.put("q","q");
	unambiguousConsonantMap.put("k","k");
	//unambiguousConsonantMap.put("l","l");
	unambiguousConsonantMap.put("m","m");
	unambiguousConsonantMap.put("n","n");
	unambiguousConsonantMap.put("h","h");

	unambiguousConsonantMap.put("'","<"); //check
	unambiguousConsonantMap.put("1","<"); //check
	unambiguousConsonantMap.put("I","<");
	unambiguousConsonantMap.put("O","<");
	unambiguousConsonantMap.put("W","<");
	unambiguousConsonantMap.put("}","<");
	//??unambiguousConsonantMap.put("{","<");
	//??unambiguousConsonantMap.put("2","<");

	HashMap<String, String[][]> vowelMap = new HashMap<String, String[][]>();
	vowelMap.put("A",new String[][] { {"aa", ""}, {"AA", ""} } );
	vowelMap.put("Y",new String[][] { {"aa", ""}, {"AA", ""} } );
	vowelMap.put("w",new String[][] { {"uu0", "uu1"}, {"UU0", "UU1"} } );
	vowelMap.put("y",new String[][] { {"ii0", "ii1"}, {"II0", "II1"} } );
	vowelMap.put("a",new String[][] { {"a"}, {"A"} } );
	vowelMap.put("u",new String[][] { {"u0", "u1"}, {"U0", "U1"} } );
	vowelMap.put("i",new String[][] { {"i0", "i1"}, {"I0", "I1"} } );


	HashMap<String, String[]> ambiguousConsonantMap = new HashMap<String, String[]>();
	ambiguousConsonantMap.put("l",new String[] {"l", ""});
	ambiguousConsonantMap.put("w",new String[] {"w"});
	ambiguousConsonantMap.put("y",new String[] {"y"});
	ambiguousConsonantMap.put("p",new String[] {"t", ""});

	/*
	  unused

	HashMap<String, String[][]> maddaMap = new HashMap<String, String[][]>();
	maddaMap.put("1",new String[][] { {"<", "aa"}, {"<", "AA"} } );

	HashMap<String, String[][]> nunationMap = new HashMap<String, String[][]>();
	nunationMap.put("F",new String[][] { {"a", "n"}, {"A", "n"} } );
	nunationMap.put("N",new String[][] { {"u1", "n"}, {"U1", "n"} } );
	nunationMap.put("K",new String[][] { {"i1", "n"}, {"I1", "n"} } );

	*/

	String diacritics = "oauiFNK_"; //check _ used for shadda
	String diacriticsWithoutShadda = "oauiFNK";
	String emphatics = "SDTZgxq";
	String forwardEmphatics = "gx";
	String consonants = "'1OWI}btvjHxd*rzs$SDTZEgklmnh";


	//First cleanup buckwalter a bit..

	text = text.replace("AF","F");
	//HB tatweel (but I've used _ for shadda.. this won't do, use = for shadda?)
	//text = text.replace("_", "");
	text = text.replace("o", "");
	text = text.replace("aA", "A");
	text = text.replace("aY", "Y");
	text = text.replace(" A", " "); //removes initial A, is it right to do it here? NO
	text = text.replace("F", "an");
	text = text.replace("N", "un");
	text = text.replace("K", "in");
	text = text.replace("1", "OA"); //??

	//end cleanup

	String phonemes = "";

	String letter;
	String prev;
	String prevprev;
	String next;
	String nextnext;
	boolean emphaticContext = false;

	text = "##"+text.trim()+"##";

	for (int i = 2; i < text.length()-2; i++) {

	    letter = Character.toString(text.charAt(i));
	    prev = Character.toString(text.charAt(i-1));
	    prevprev = Character.toString(text.charAt(i-2));
	    next = Character.toString(text.charAt(i+1));
	    nextnext = Character.toString(text.charAt(i+2));

	    String phone = "";
	    

	    //set emphatic context
	    if ( letter.matches("["+consonants+"wy"+"]") && !letter.matches("["+emphatics+"rl"+"]") ) {
		emphaticContext = false;
	    } else if ( letter.matches("["+emphatics+"]") ) {
		emphaticContext = true;
	    } else if ( next.matches("["+emphatics+"]") && !next.matches("["+forwardEmphatics+"]") ) {
		emphaticContext = true;
	    }

	    //If letter is unambiguous
	    if ( unambiguousConsonantMap.containsKey(letter) ) {
		//System.out.println("Unambiguous Consonant: "+letter+", next: "+next);

		// double if next is shadda TODO change shadda symbol
		if ( next.equals("_") ) {
		    //System.out.println("Next is shadda");
		    phone = unambiguousConsonantMap.get(letter)+unambiguousConsonantMap.get(letter);
		} else {
		    phone = unambiguousConsonantMap.get(letter);
		}
	    }

	    // A in definite article should be a if initial in phrase (it would be nice to be able to drop it here if it is not..)
	    else if ( letter.equals("A") && next.equals("l") ) {
		phone = "a";
	    }

	    //#Lam could be omitted in definite article (sun letters with shadda) TODO shadda is still "_", change
	    else if ( letter.equals("l") ) {
		//System.out.println("al: "+next+", "+nextnext);
		if ( prev.equals("A") && !next.matches("["+diacritics+"]") && !vowelMap.containsKey(next) && ( nextnext.equals("_") ) ) {
		    phone = "";
		} else {
		    phone = "l";
		}
	    }

	    //use maddaMap ??
	    //#Ta' marboota is determined by the following if it is a diacritic or not

	    else if ( letter.equals("p") ) {
		//System.out.println("p: "+next);
		if ( next.matches("["+diacritics+"]") ) {
		    phone = "t";
		} else {
		    phone = "";
		}
	    }


	    //vowels (at first really basic!)
	    else if ( vowelMap.containsKey(letter) ) {
		//System.out.println("Vowel: "+letter);

		//phone = vowelMap.get(letter)[0][0];

		//Now copying rules from Arabic-Phonetiser
		//TODO add examples
		if ( letter.matches("[wy]") ) {
		    if ( next.matches("["+diacriticsWithoutShadda+"AY"+"]")
			 || ( next.matches("[wy]") && !nextnext.matches("["+diacritics+"Awy"+"]") ) 
			 || ( prev.matches("["+diacriticsWithoutShadda+"]") && next.matches("["+consonants+"#"+"]") )
			 //|| ( prev.matches("[oauiFNK]") && next.matches("['1OWI}btvjHxd*rzs$SDTZEgklmnh#]") )
			 ) {
			if ( ( letter.equals("w") && prev.equals("u") && !next.matches("[aiAY]") ) || ( letter.equals("y") && prev.equals("i") && !next.matches("[aiAY]") ) ) {
			    if ( emphaticContext ) {
				phone = vowelMap.get(letter)[1][0];
				//System.out.println("Vowel case 1: "+letter+" -> "+phone); //UU0, II0
			    } else {
				phone = vowelMap.get(letter)[0][0];
				//System.out.println("Vowel case 2: "+letter+" -> "+phone); //uu0, ii0
			    }
			} else {
			    if ( letter.equals("w") && next.equals("A") && nextnext.equals("#") ) {
				phone = "w uu0";
				//System.out.println("Vowel case 3: "+letter+" -> "+phone); //w uu0 TODO maybe NH means that these are options?
			    } else {
				phone = ambiguousConsonantMap.get(letter)[0];
				//System.out.println("Vowel case 4: "+letter+" -> "+phone); //w, y
			    }
			}
			
		    } else if ( next.equals("_") ) {
			//TODO change shadda
			if ( prev.equals("a") || ( letter.equals("w") && prev.matches("[iy]") ) || ( letter.equals("y") && prev.matches("[uw]") ) ) {
			    phone = ambiguousConsonantMap.get(letter)[0]+ambiguousConsonantMap.get(letter)[0];
			    //System.out.println("Vowel case 5: "+letter+" -> "+phone); //ww, yy
			} else {
			    phone = vowelMap.get(letter)[0][0]+ambiguousConsonantMap.get(letter)[0];
			    //System.out.println("Vowel case 6: "+letter+" -> "+phone); //uu0 w, ii0 y TODO maybe NH means that these are options?
			}
		    } else {
			//#Waws and Ya's at the end of the word could be shortened
			if ( emphaticContext ) {
			    if ( prev.matches("["+consonants+"ui"+"]") && next.equals("#") ) {
				phone = vowelMap.get(letter)[1][0];
				phone = phone.replaceAll("^.",""); //remove first character
				//System.out.println("Vowel case 7: "+letter+" -> "+phone); //U0, I0
			    } else {
				phone = vowelMap.get(letter)[1][0];
				//System.out.println("Vowel case 8: "+letter+" -> "+phone); //UU0, II0
			    }
			} else {
			    if ( prev.matches("["+consonants+"ui"+"]") && next.equals("#") ) {
				phone = vowelMap.get(letter)[0][0];
				phone = phone.replaceAll("^.",""); //remove first character
				//System.out.println("Vowel case 9: "+letter+" -> "+phone); //u0, i0
			    } else {
				phone = vowelMap.get(letter)[0][0];
				//System.out.println("Vowel case 10: "+letter+" -> "+phone); //uu0, ii0
			    }
			}
		    }
		} else if ( letter.matches("[ui]") ) {
		    //#Kasra and Damma could be mildened if before a final silent consonant (HB what does this mean? Example!)
		    if ( emphaticContext ) {
			if ( ( unambiguousConsonantMap.containsKey(next) || next.equals("l") ) && nextnext.equals("#") && text.length() > 7 ) {
			    phone = vowelMap.get(letter)[1][1];
			    //System.out.println("Vowel case 11: "+letter+" -> "+phone); //??
			} else {
			    phone = vowelMap.get(letter)[1][0];
			    //System.out.println("Vowel case 12: "+letter+" -> "+phone); //??
			}
		    } else {
			if ( ( unambiguousConsonantMap.containsKey(next) || next.equals("l") ) && nextnext.equals("#") && text.length() > 7 ) {
			    phone = vowelMap.get(letter)[0][1];
			    //System.out.println("Vowel case 13: "+letter+" -> "+phone); //??
			} else {
			    phone = vowelMap.get(letter)[0][0];
			    //System.out.println("Vowel case 14: "+letter+" -> "+phone); //??
			}
		    }
		} else if ( letter.matches("[aAY]") ) {
		    //#Alif could be ommited in definite article and beginning of some words
		    if ( letter.equals("A") && prev.matches("[wk]") && prevprev.equals("#") ) {
			phone = "a";
			//System.out.println("Vowel case 15: "+letter+" -> "+phone); //??
		    } else if ( letter.equals("A") && prev.matches("[ui]") ) {
			phone = ""; //???
			//System.out.println("Special case: A dropped after u|i");
		    } else if ( letter.equals("A") && prev.equals("w") && next.equals("#") ) {
			phone = "";
			//System.out.println("Vowel case 16: "+letter+" -> "+phone); //??
		    } else if ( letter.matches("[AY]") && next.equals("#") ) {
			if ( emphaticContext ) {
			    phone = "A";
			    //System.out.println("Vowel case 17: "+letter+" -> "+phone); //??
			} else {
			    phone = "a";
			    //System.out.println("Vowel case 18: "+letter+" -> "+phone); //??
			}
		    } else {
			if ( emphaticContext ) {
			    phone = vowelMap.get(letter)[1][0];
			    //System.out.println("Vowel case 19: "+letter+" -> "+phone); //??
			} else {
			    phone = vowelMap.get(letter)[0][0];
			    //System.out.println("Vowel case 20: "+letter+" -> "+phone); //??
			}
		    }


		}


	    }

	    




	    else {
		System.out.println("WARNING: (JPhonemiser) No match for letter: "+letter);
	    }


	    //System.out.println("letter: \""+letter+"\" --> phone: "+phone);
	    phonemes += " "+phone;
			    
	}

	//cleanup a bit..
	phonemes = phonemes.replace("i0 ii0", "ii0");

	//System.out.println("Word: "+text+" --> "+phonemes);


	return phonemes;
    }




}
