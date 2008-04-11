/**
 * 
 */
package ecologylab.semantics.library;

import ecologylab.generic.Debug;
import ecologylab.semantics.library.scholarlyPublication.Author;
import ecologylab.semantics.library.scholarlyPublication.Reference;
import ecologylab.semantics.library.scholarlyPublication.AcmPortal;
import ecologylab.semantics.library.scholarlyPublication.Source;
import ecologylab.xml.TranslationScope;

/**
 * 
 * 
 * @author bharat
 * 
 */
public class DefaultMetadataTranslationSpace extends Debug
{
	public static final String NAME = "defaultMetadataTranslationSpace";
	public static final String PACKAGE_NAME = "defaultMetadataTranslationSpace";
	
	protected static final Class TRANSLATIONS[] = 
	{
//		Dlms.class,
		DcDocument.class,
		Document.class,
		Media.class,
		Flickr.class,
//		Icdl.class,
		IcdlImage.class,
		Image.class,
//		Nsdl.class,
		Rss.class,
		Search.class,
		Text.class,
		Pdf.class,
		Author.class,
		Reference.class,
		Source.class,
		AcmPortal.class
	};

	public static TranslationScope get()
	{
		return TranslationScope.get(PACKAGE_NAME, TRANSLATIONS);
	}
	
}
