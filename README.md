# marytts-lang-ar

This is very much work in progress.
It sort of works for me - but there are plenty of errors and problems.

Test version is (sometimes - and apparently works in chrome only) up on http://morf.se:59125

For vocalisation another server needs to be running in the background. Clone it from here https://github.com/HaraldBerthelsen/arabic_vocaliser.git, and run "nohup python vocalise.py server &" 

Clone and compile marytts (mvn package).

Then clone and compile this, copy the jarfile to marytts/target/marytts-5.2-SNAPSHOT/lib.

Also for now the jar file lib/javax-json-1.0.jar needs to be copied into the marytts target lib directory. (This should be done automatically with maven)

The first version test voice can hopefully be released soon as well.


Next steps:

Use standard Buckwalter, or clearly document any deviations. Some characters cause problems, eg. ~ ' } are tokenised as separate words.

Fix problems with phonetiser rules. Some rules are clearly wrong - test examples (or a native speaker!) needed to sort them out.

Include java code from https://github.com/bluemix/NumberToArabicWords.git for number conversion.

Convert vocaliser from python to java - or at least speed it up and fix problems.
