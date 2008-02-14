package ecologylab.semantics.library;

/**
 * 
 * @author damaraju
 *
 */
public class Rss extends Document
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
