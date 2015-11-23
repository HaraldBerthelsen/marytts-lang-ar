# marytts-lang-ar

This is very much work in progress.
It sort of works for me - but there are plenty of errors and problems.

Test version is (sometimes) up on http://demo.morf.se/marytts

For vocalisation the Mishkal software needs to be running in the background. Clone it from here https://github.com/linuxscout/mishkal.git, and run mishkal-webserver.py

Next steps:

I have a strange problem with marytts: This works fine for me on my local computer, but on our server it doesn't work. Two java classes used in marytts-lang-ar, TextToRawMaryXML and Postlex, are never called on the server. I don't see what the difference is, the code is the same. It's probably something really obvious that I'm just missing. But this means that no vocalisation or conversion to buckwalter is run on the server, and only buckwalter input works. Also assimilation of definite article doesn't work, because it is run in Postlex. TODO: first test it on a third machine.

Use standard Buckwalter, or clearly document any deviations. Some characters cause problems, eg. ~ ' } are tokenised as separate words.

Fix problems with phonetiser rules. Some rules are clearly wrong - test examples (or a native speaker!) needed to sort them out.

Include java code from https://github.com/bluemix/NumberToArabicWords.git for number conversion.