package ecologylab.semantics.library;

import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.xml.ElementState.xml_nested;

/**
 * 
 * @author damaraju
 *
 */
public class Icdl extends Document
{
//	@xml_attribute String		languages;
//	@xml_nested MetadataString	languages = new MetadataString();
	@xml_nested MetadataString	languages;
	MetadataString languages()
	{
		MetadataString result = this.languages;
		if(result == null)
		{
			result 			= new MetadataString();
			this.languages 	= result;
		}
		return result;
	}
	
	public String getLanguages()
	{
//		return languages;
		return languages().getValue();
	}

	public void hwSetLanguages(String languages)
	{
//		this.languages = languages;
		this.languages().setValue(languages);
		rebuildCompositeTermVector();
	}
	
	public void setLanguages(String languages)
	{
//		this.languages = languages;
		this.languages().setValue(languages);
	}
	
	
}
