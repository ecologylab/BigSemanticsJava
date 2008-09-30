/**
 * 
 */
package ecologylab.semantics.library.uva;

import java.util.ArrayList;
import java.util.HashMap;

import ecologylab.semantics.library.scalar.MetadataInteger;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.metadata.Metadata;

/**
 * @author vdeboer
 *
 */
public class Topic extends Metadata
{
	@xml_attribute MetadataInteger id;
	
	@xml_map("keyword_set") HashMap<MetadataString, ArrayList<String>> keywordSet; 
}
