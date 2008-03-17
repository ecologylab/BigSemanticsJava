package ecologylab.semantics.library;

/**
 * 
 * @author damaraju
 *
 */
public class Icdl extends Search
{
	@xml_attribute String		languages;

	public String getLanguages()
	{
		return languages;
	}

	public void setLanguages(String languages)
	{
		this.languages = languages;
		rebuildCompositeTermVector();
	}
	
	public void lwSetLanguages(String languages)
	{
		this.languages = languages;
	}
	
	
}
