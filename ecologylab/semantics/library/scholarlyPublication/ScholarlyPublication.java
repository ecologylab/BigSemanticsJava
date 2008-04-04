package ecologylab.semantics.library.scholarlyPublication;

import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.xml_inherit;

/**
 * 
 * @author damaraju
 *
 */
@xml_inherit
public class ScholarlyPublication extends Metadata
{
	@xml_leaf 	String 					title;
	@xml_leaf 	ParsedURL				fullText;
	@xml_nested	Source					source;
	@xml_map Author						author;
	@xml_map Reference					reference;
	@xml_map Citation					citation;
	

}
