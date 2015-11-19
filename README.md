# marytts-lang-ar

This is very much work in progress. 
It works for me - but there are plenty of errors and problems.
For vocalisation the Mishkal software needs to be running in the background. Clone it from here https://github.com/linuxscout/mishkal.git, and run mishkal-webserver.py

Next steps:
Use "standard" Buckwalter or document any deviations. Some characters cause problems, eg. ~ ' } are tokenised as separate words.
Fix problems with phonetiser rules.
Include java code from https://github.com/bluemix/NumberToArabicWords.git for number conversion.