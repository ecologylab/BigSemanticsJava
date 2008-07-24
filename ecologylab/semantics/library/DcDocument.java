/**
 * 
 */
package ecologylab.semantics.library;

import ecologylab.xml.xml_inherit;
import ecologylab.semantics.library.scalar.MetadataString;

/**
 * @author bharat
 *
 */
@xml_inherit
public class DcDocument extends Document
{
	@xml_nested MetadataString	subject;
	
	//Efficient retrieval through lazy evaluation.
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
		this.setSubject(subject);
		rebuildCompositeTermVector();
	}
	
	public void setSubject(String subject)
	{
		this.subject().setValue(subject);
	}
}
