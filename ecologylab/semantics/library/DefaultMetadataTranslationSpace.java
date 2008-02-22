/**
 * 
 */
package ecologylab.semantics.library;

import ecologylab.generic.Debug;
import ecologylab.xml.TranslationSpace;

/**
 * 
 * 
 * @author Bharat Bandaru
 * 
 */
public class DefaultMetadataTranslationSpace extends Debug
{
	public static final String NAME = "defaultMetadataTranslationSpace";
	public static final String PACKAGE_NAME = "defaultMetadataTranslationSpace";
	
	protected static final Class TRANSLATIONS[] = 
	{
		Dlms.class,
		Document.class,
		Flickr.class,
		Icdl.class,
		Image.class,
		Nsdl.class,
		Rss.class,
		Search.class,
		Text.class
	};

	public static TranslationSpace get()
	{
		return TranslationSpace.get(PACKAGE_NAME, TRANSLATIONS);
	}
	
}
