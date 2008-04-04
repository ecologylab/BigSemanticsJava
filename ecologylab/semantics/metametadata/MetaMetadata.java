/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.util.ArrayList;

import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.DefaultMetadataTranslationSpace;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.xml_inherit;

/**
 * @author damaraju
 *
 */
public class MetaMetadata extends MetaMetadataField
{
	@xml_attribute private 	String 		urlBase;
	
	TranslationScope 					translationScope;
	
	boolean								doNotTranslateToJava;
	
	@xml_collection("mixins")  ArrayList<String> 		mixins;
	
	public MetaMetadata()
	{
		super();
	}

	public boolean isSupported(ParsedURL purl)
	{
		return purl.toString().startsWith(urlBase);
	}

	public String getUrlBase() {
		return urlBase;
	}

	public void setUrlBase(String urlBase) {
		this.urlBase = urlBase;
	}

	public TranslationScope getTS() {
		return translationScope;
	}

	public void setTS(TranslationScope ts) {
		translationScope = ts;
	}
	
	TranslationScope DEFAULT_METADATA_TRANSLATIONS	= DefaultMetadataTranslationSpace.get();
	
	/**
	 * Lookup the Metadata class object that corresponds to the tag_name in this.
	 * @return
	 */
	public Class<? extends Metadata> getMetadataClass()
	{
		return getMetadataClass(name);
	}
	public Class<? extends Metadata> getMetadataClass(String name)
	{
		return (Class<? extends Metadata>) DEFAULT_METADATA_TRANSLATIONS.getClassByTag(name);
	}
	

	/**
	 * Lookup the Metadata class that corresponds to the (tag) name of this, using the DefaultMetadataTranslationSpace.
	 * Assuming that is found, use reflection to instantiate it.
	 * 
	 * @return	An instance of the Metadata subclass that corresponds to this, or null, if there is none.
	 */
	public Metadata constructMetadata()
	{
		Metadata result	= null;
		Class<? extends Metadata> metadataClass	= getMetadataClass();
		
		
		if (metadataClass != null)
		{
			result		= ReflectionTools.getInstance(metadataClass);
			result.setMetaMetadata(this);
			if(mixins != null && mixins.size() > 0)
			{
				for(String mixinName : mixins)
				{
					Class<? extends Metadata> mixinClass = getMetadataClass(mixinName);
					if(mixinClass != null)
					{
						result.addMixin(ReflectionTools.getInstance(mixinClass));
					}
				}
			}
		}
		return result;
	}

		
}
