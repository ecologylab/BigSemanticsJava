/**
 * 
 */
package ecologylab.semantics.library;

import ecologylab.xml.ElementState.xml_attribute;

/**
 * @author bharat
 *
 */
public class IcdlImage extends Image
{
	@xml_attribute String		languages;

	public String getLanguages()
	{
		return languages;
	}

	public void setLanguages(String languages)
	{
		this.languages = languages;
	}
	
	public void setLwLanguages(String languages)
	{
		this.languages = languages;
	}
}
