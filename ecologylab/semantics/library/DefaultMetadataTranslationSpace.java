/**
 * 
 */
package ecologylab.semantics.library;

import ecologylab.generic.Debug;
import ecologylab.semantics.library.scalar.MetadataInteger;
import ecologylab.semantics.library.scalar.MetadataParsedURL;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.library.scalar.MetadataStringBuilder;
import ecologylab.semantics.library.scholarlyPublication.Author;
import ecologylab.semantics.library.scholarlyPublication.Reference;
import ecologylab.semantics.library.scholarlyPublication.AcmPortal;
import ecologylab.semantics.library.scholarlyPublication.Source;
import ecologylab.semantics.metadata.DebugMetadata;
import ecologylab.semantics.metadata.Media;
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
		MetadataString.class,
		MetadataStringBuilder.class,
		MetadataParsedURL.class,
		MetadataInteger.class,
		DebugMetadata.class,
		DcDocument.class,
		Document.class,
		Media.class,
		Flickr.class,
		IcdlImage.class,
		Image.class,
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
