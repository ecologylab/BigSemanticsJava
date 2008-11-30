/**
 * 
 */
package ecologylab.semantics.library;

import ecologylab.generic.Debug;
import ecologylab.semantics.library.scalar.MetadataInteger;
import ecologylab.semantics.library.scalar.MetadataParsedURL;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.library.scalar.MetadataStringBuilder;
import ecologylab.semantics.metadata.DebugMetadata;
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
		DebugMetadata.class,
		MetadataString.class,
		MetadataStringBuilder.class,
		MetadataParsedURL.class,
		MetadataInteger.class,
		
				
	};

	public static TranslationScope get()
	{
		return TranslationScope.get(PACKAGE_NAME, TRANSLATIONS);
	}
	
}
