/**
 * 
 */
package ecologylab.semantics.library;

import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_attribute;
import ecologylab.semantics.metadata.Metadata;

/**
 * @author bharat
 *
 */
@xml_inherit
public class DcDocument extends Document
{
	@xml_attribute 	String		subject;

	public String getSubject()
	{
		return subject;
	}

	public void hwSetSubject(String subject)
	{
		this.subject = subject;
		rebuildCompositeTermVector();
	}
	
	public void setSubject(String subject)
	{
		this.subject = subject;
	}
}
