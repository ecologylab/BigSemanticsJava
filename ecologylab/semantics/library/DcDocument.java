/**
 * 
 */
package ecologylab.semantics.library;

import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_attribute;
import ecologylab.xml.ElementState.xml_nested;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.metadata.Metadata;

/**
 * @author bharat
 *
 */
@xml_inherit
public class DcDocument extends Document
{
//	@xml_attribute 	String		subject;
//	@xml_nested MetadataString	subject = new MetadataString();
	@xml_nested MetadataString	subject;
	
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
//		this.subject = subject;
		this.subject().setValue(subject);
		rebuildCompositeTermVector();
	}
	
	public void setSubject(String subject)
	{
//		this.subject = subject;
		this.subject().setValue(subject);
	}
}
