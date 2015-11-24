# marytts-lang-ar

This is very much work in progress.
It sort of works for me - but there are plenty of errors and problems.

Test version is (sometimes - and apparently works in chrome only) up on http://demo.morf.se/marytts

For vocalisation the Mishkal software needs to be running in the background. Clone it from here https://github.com/linuxscout/mishkal.git, and run mishkal-webserver.py. 
NOTE - the current version 2015-11-24 of mishkal doesn't seem to work at all, the code is being restructured. I have a version from a week ago or so that works, and I'm sure it will work again soon. 

Clone and compile marytts (mvn package).

Then clone and compile this, copy the jarfile to marytts/target/marytts-5.2-SNAPSHOT/lib.

Also for now the jar file lib/javax-json-1.0.jar needs to be copied into the marytts target lib directory. (This should be done automatically with maven)

The first version test voice can hopefully be released soon as well.


Next steps:

Use standard Buckwalter, or clearly document any deviations. Some characters cause problems, eg. ~ ' } are tokenised as separate words.

Fix problems with phonetiser rules. Some rules are clearly wrong - test examples (or a native speaker!) needed to sort them out.

Include java code from https://github.com/bluemix/NumberToArabicWords.git for number conversion.

Use (or develop) a java vocalizer to avoid dependency on the mishkal server.
