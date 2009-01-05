# current working directory

INTERACTIVE_DIRS = actions library library/scalar gui metametadata metadata metadata/extraction model/text 
DIRS = $(INTERACTIVE_DIRS)
JAR_DIRS = $(INTERACTIVE_DIRS:%=ecologylab/semantics/%) ecologylab/documenttypes

EXTRA = ../ecologylabFundamental; ../ecologylabGeneratedSemantics

MAKE_DIR = ../../makefiles
include $(MAKE_DIR)/java.make

TARGET		= ecologylabSemantics
