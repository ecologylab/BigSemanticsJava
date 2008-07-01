package ecologylab.semantics.library;

import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_nested;

/**
 * 
 * @author damaraju
 *
 */
@xml_inherit
public class Dlms extends Document
{

	@xml_nested MetadataString	subject;
	
	//Lazy evaluation for efficient retrieval.
	MetadataString subject()
	{
		MetadataString result = this.subject;
		if(result == null)
		{
			result 			= new MetadataString();
			this.subject 	= result;
		}
		return result;
	}
	
	public String getSubject()
	{
		return subject().getValue();
	}

	public void hwSetSubject(String subject)
	{
		this.subject().setValue(subject);
		rebuildCompositeTermVector();
	}
	
	public void setSubject(String subject)
	{
		this.subject().setValue(subject);
	}
}
