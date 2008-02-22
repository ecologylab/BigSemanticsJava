/**
 * 
 */
package ecologylab.semantics.metametadata;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.DefaultMetadataTranslationSpace;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.TranslationSpace;
import ecologylab.xml.xml_inherit;

/**
 * @author damaraju
 *
 */
public class MetaMetadata extends MetaMetadataField
{
	
	@xml_attribute 			String 		name;
	@xml_attribute private 	String 		urlBase;
	
	TranslationSpace TS;

	public MetaMetadata()
	{
		super();
	}

	public boolean isSupported(ParsedURL purl)
	{
		return purl.toString().startsWith(urlBase);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrlBase() {
		return urlBase;
	}

	public void setUrlBase(String urlBase) {
		this.urlBase = urlBase;
	}

	public TranslationSpace getTS() {
		return TS;
	}

	public void setTS(TranslationSpace ts) {
		TS = ts;
	}
	
	/**
	 * This will return the class object using it's tag name
	 * @param metadataClassName
	 * @return
	 */
	public Class<? extends Metadata> getMetadataClass(String metadataClassName)
	{
		TranslationSpace translationSpace = DefaultMetadataTranslationSpace.get();
		
		return (Class<? extends Metadata>) translationSpace.getClassByTag(metadataClassName);
	}
}
