package ecologylab.semantics.library;

public class Nsdl extends Search
{
	@xml_attribute String		Subject;

	public String getSubject()
	{
		return Subject;
	}

	public void setSubject(String subject)
	{
		Subject = subject;
	}
	
}
