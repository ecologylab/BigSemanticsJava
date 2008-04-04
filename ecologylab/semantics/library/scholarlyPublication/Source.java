package ecologylab.semantics.library.scholarlyPublication;

import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;



public class Source extends Metadata
{
	@xml_leaf String		heading;
	@xml_leaf ParsedURL		archive;
	@xml_leaf ParsedURL		tableOfContents;
	@xml_leaf String		pages;
	@xml_leaf String		yearOfPublication;
	

}
