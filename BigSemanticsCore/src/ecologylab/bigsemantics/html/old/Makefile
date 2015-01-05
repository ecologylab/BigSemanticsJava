# current working directory
DIR	= html

JAVA_SRC = TextToken.java Scan.java \
	   Backend.java Parser.java

JAVA_ROOT= ../../..

MAKE_DIR = ../../../../../makefiles
include $(MAKE_DIR)/applets.make

FROM_ROOT= source/$(DIR)

scan:	Scan.class
	$(JAVA) cm.media.html.Scan y:/people/andruid/research/dissertation/reports/yahoo-wolves.txt

parser:	Parser.class
	$(JAVA) cm.media.html.Parser

scannet:	Scan.class
	$(JAVA) source.html.Scan "http://search.yahoo.com/bin/search?n=50&p=wolves"

#	$(JAVA) source.html.Scan http://search.yahoo.com/search?n=50&p=wolves&za=default

google:	Scan.class
	$(JAVA) source.html.Scan "http://www.google.com/search?q=fractals&meta=lr%3D%26hl%3Den"

g:	Scan.class
	$(JAVA) source.html.Scan "http://www.google.com/search?num=10&meta=hl%3Den%26lr%3D&safe=off&q=Duchamp"

#	$(JAVA) source.html.Scan http://search.yahoo.com/search?n=50&p=wolves&za=default

msn:
	$(JAVA) source.html.Scan "http://home.microsoft.com/Default.asp?MSID=91152601e31811d298840008c7d9e3db"

cabsign: all

nsjar: all

