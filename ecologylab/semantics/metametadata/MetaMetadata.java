/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.DefaultMetadataTranslationSpace;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTranslationException;

/**
 * @author damaraju
 *
 */
public class MetaMetadata extends MetaMetadataField
{
	@xml_attribute ParsedURL 	 						urlBase;
	
	@xml_collection("mixins")  ArrayList<String> 		mixins;

	@xml_attribute String								userAgentName;
	
	@xml_attribute String								userAgentString;
	
	TranslationScope 									translationScope;
	
	boolean												doNotTranslateToJava;
	
	public MetaMetadata()
	{
		super();
	}

	public boolean isSupported(ParsedURL purl)
	{
		return purl.toString().startsWith(urlBase.toString());
	}
	
	public String getUserAgent()
	{
		return userAgentName;
	}

	public ParsedURL getUrlBase() {
		return urlBase;
	}

	public void setUrlBase(ParsedURL urlBase) {
		this.urlBase = urlBase;
	}
	
	public void setUrlBase(String urlBase) {
		this.urlBase = ParsedURL.getAbsolute(urlBase);
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

	@Override
	public void translateToMetadataClass(Appendable appendable, File outputRoot) 
	throws XMLTranslationException
	{
		if (appendable == null)
	        throw new XMLTranslationException("Appendable is null");
	
		try
		{
			super.translateToMetadataClass(appendable, outputRoot);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public MetaMetadataRepository repository()
	{
		return (MetaMetadataRepository) parent();
	}
	
	public String getUserAgentString()
	{
		if (userAgentString == null)
		{
			userAgentString = (userAgentName == null) ? repository().getDefaultUserAgentString() :
				repository().getUserAgentString(userAgentName);
		}
		
		return userAgentString;
	}
	
	public static void main(String args[]) throws XMLTranslationException
	{
		final TranslationScope TS = MetaMetadataTranslationScope.get();
		String patternXMLFilepath = "../cf/config/semantics/metametadata/defaultRepository.xml";

//		ElementState.setUseDOMForTranslateTo(true);
		MetaMetadataRepository test = (MetaMetadataRepository) ElementState.translateFromXML(patternXMLFilepath, TS);
		
		//test.writePrettyXML(System.out);
		
		File outputRoot	= PropertiesAndDirectories.userDir();
		
		for (MetaMetadata metaMetadata : test.values())
		{
			metaMetadata.translateToMetadataClass(System.out, outputRoot);
			System.out.println('\n');
		}
	}
	
}
