# current working directory

INTERACTIVE_DIRS = library library/scholarlyPublication gui library/scalar metametadata metadata metadata/extraction model/text
DIRS = $(INTERACTIVE_DIRS)
JAR_DIRS = $(INTERACTIVE_DIRS:%=ecologylab/semantics/%)

EXTRA = ../ecologylabFundamental

MAKE_DIR = ../../makefiles
include $(MAKE_DIR)/java.make

TARGET		= ecologylabSemantics
