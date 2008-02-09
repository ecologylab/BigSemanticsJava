package ecologylab.semantics.library;

import ecologylab.xml.xml_inherit;


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
