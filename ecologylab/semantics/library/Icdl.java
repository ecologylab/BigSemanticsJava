package ecologylab.semantics.library;

/**
 * 
 * @author damaraju
 *
 */
public class Icdl extends Document
{
	@xml_attribute String		languages;

	public String getLanguages()
	{
		return languages;
	}

	public void hwSetLanguages(String languages)
	{
		this.languages = languages;
		rebuildCompositeTermVector();
	}
	
	public void setLanguages(String languages)
	{
		this.languages = languages;
	}
	
	
}
