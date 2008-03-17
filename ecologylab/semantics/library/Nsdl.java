package ecologylab.semantics.library;

/**
 * 
 * @author damaraju
 *
 */
public class Nsdl extends Search
{
	@xml_attribute String		subject;

	public String getSubject()
	{
		return subject;
	}

	public void setSubject(String subject)
	{
		subject = subject;
		rebuildCompositeTermVector();
	}
	
	public void lwSetSubject(String subject)
	{
		subject = subject;
	}
	
}
