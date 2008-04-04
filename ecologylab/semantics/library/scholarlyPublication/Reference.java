package ecologylab.semantics.library.scholarlyPublication;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;

/**
 * 
 * @author damaraju
 *
 */
public class Reference extends Metadata 
{
	@xml_leaf String 		referenceText;
	@xml_leaf ParsedURL		referenceUrl;

}
