# current working directory

SEMANTICS_DIRS = actions library library/scalar gui metametadata metadata metadata/extraction model/text 
MEDIA_DIRS = media/html media/html/dom media/html/dom/documentstructure documenttypes 
DIRS = $(SEMANTICS_DIRS)
JAR_DIRS = $(SEMANTICS_DIRS:%=ecologylab/semantics/%) $(MEDIA_DIRS:%=ecologylab/%)

EXTRA = ../ecologylabFundamental; ../ecologylabGeneratedSemantics

MAKE_DIR = ../../makefiles
include $(MAKE_DIR)/java.make

TARGET		= ecologylabSemantics
