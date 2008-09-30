/**
 * 
 */
package ecologylab.semantics.library.uva;

import java.util.ArrayList;

import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.library.scalar.MetadataInteger;
import ecologylab.semantics.metadata.Metadata;

/**
 * @author vdeboer
 *
 */
public class Topic extends Metadata
{
	@xml_attribute MetadataInteger id;
	
	@xml_map("keyword_set") HashMapArrayList<String, ArrayList<String>> keywordSet; 
}
