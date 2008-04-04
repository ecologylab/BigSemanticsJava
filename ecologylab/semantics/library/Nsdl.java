package ecologylab.semantics.library;

/**
 * 
 * @author damaraju
 *
 */
public class Nsdl extends Document
{
	@xml_attribute String		subject;

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
