# marytts-lang-ar

This is very much work in progress.
It sort of works for me - but there are plenty of errors and problems.

Test version is (sometimes) up on http://demo.morf.se/marytts

For vocalisation the Mishkal software needs to be running in the background. Clone it from here https://github.com/linuxscout/mishkal.git, and run mishkal-webserver.py
Also for now the jar file lib/javax-json-1.0.jar needs to be copied into the marytts target lib directory. (This should be done automatically with maven)

Next steps:

Use standard Buckwalter, or clearly document any deviations. Some characters cause problems, eg. ~ ' } are tokenised as separate words.

Fix problems with phonetiser rules. Some rules are clearly wrong - test examples (or a native speaker!) needed to sort them out.

Include java code from https://github.com/bluemix/NumberToArabicWords.git for number conversion.