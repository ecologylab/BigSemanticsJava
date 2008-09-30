/**
 * 
 */
package ecologylab.semantics.library.uva;

import java.util.ArrayList;

import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.library.Document;
import ecologylab.semantics.library.scalar.MetadataInteger;

/**
 * @author vdeboer
 *
 */
public class Topic extends Document
{
	@xml_attribute MetadataInteger id;
	
	@xml_collection("content_keywords") ArrayList<String> contentKeys;
	@xml_collection("content_keywords") ArrayList<String> anchorKeys;
	@xml_collection("content_keywords") ArrayList<String> titleKeys;
	@xml_collection("content_keywords") ArrayList<String> urlKeys;
	
}
