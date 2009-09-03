package ecologylab.semantics.library;

import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_nested;

/**
 * 
 * @author damaraju
 *
 */
@xml_inherit
public class Icdl extends Document
{
	@xml_nested MetadataString	languages;
	
	//Lazy evaluation for efficient retrieval.
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
		return languages().getValue();
	}

	public void hwSetLanguages(String languages)
	{
		this.setLanguages(languages);
		rebuildCompositeTermVector();
	}
	
	public void setLanguages(String languages)
	{
		this.languages().setValue(languages);
	}
	
}
