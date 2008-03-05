package ecologylab.semantics.library;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.xml_inherit;

/**
 * 
 * @author damaraju
 *
 */
@xml_inherit
public class Dlms extends Search
{

	@xml_attribute 	String		subject;

	public String getSubject()
	{
		return subject;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}
}
