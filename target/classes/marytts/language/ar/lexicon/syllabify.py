#To syllabify the arabic buckwalter lexicon build from the Arabic Speech Corpus by Nawar Halabi.

import sys, re


def loadAllophones(allophones_file):
    global vowel_re, vowel_list, consonant_re

    fh = open(allophones_file)
    lines = fh.readlines()
    fh.close()
    consonant_list = []
    vowel_list = []
    for line in lines:
        if re.search("vowel", line):
            m = re.search("ph=\"([^\"]+)\"", line)
            symbol = m.group(1)
            vowel_list.append(symbol)
        if re.search("consonant", line):
            m = re.search("ph=\"([^\"]+)\"", line)
            symbol = m.group(1)
            consonant_list.append(symbol)

    vowel_re = "|".join(sorted(vowel_list, key=len, reverse=True))
    consonant_re = "|".join(sorted(consonant_list, key=len, reverse=True))

def syllabify(transcription):
    #print transcription
    global vowel_re, vowel_list
    parts = re.split("("+vowel_re+")", transcription)
    syllables = []
    syllable = []
    i = 0
    for part in parts:
        i += 1
        #print "|"+part+"|"
        if part == "":
            continue
        elif part in vowel_list:

            if part.endswith("'"):
                part = part[:-1]
                syllable.insert(0,"'")

            syllable.append(part)

        elif i == len(parts):
            #don't split final consonant cluster
            syllable.append(part)

        else:
            #split consonant cluster
            if len(part) == 4:
                coda = part[0:1]
                onset = part[2:3]
            elif len(part) == 3 and part[0] != part[1]:
                coda = part[0:1]
                onset = part[2]
            elif len(part) == 3 and part[0] == part[1]:
                coda = part[0]
                onset = part[1:2]
            elif len(part) == 2 and part[0] != part[1]:
                coda = part[0]
                onset = part[1]
            elif len(part) == 2 and part[0] == part[1]:
                coda = ""
                onset = part
            elif len(part) == 1:
                coda = ""
                onset = part
            else:
                print "ERROR: %s %d" % (part, len(part))
                sys.exit()

            if coda != "":
                syllable.append(coda)
            if syllable != []:
                syllables.append("".join(syllable))
            syllable = [onset]


    if syllable != []:
        syllables.append("".join(syllable))
   
    syllabified_trans = "-".join(syllables)
    #print "%s -> %s" % (transcription, syllabified_trans)
    return syllabified_trans


loadAllophones("allophones.ar.xml")
lexlines = sys.stdin.readlines()
for lexline in lexlines:
    lexline = lexline.strip()
    (orth,trans) = lexline.split("|")
    syllabified_trans = syllabify(trans)
    print "%s|%s" % (orth,syllabified_trans)
    #sys.exit()
