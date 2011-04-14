/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.generic.Debug;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.serialization.TranslationScope;

/**
 * Encapsulates ClassDescriptors for Metadata and its subclasses that are coded by hand.
 */
public class MetadataBuiltinsTranslationScope extends Debug
{
	public static final String NAME = "metadata_builtin_translations";
	
	protected static final Class CLASSES[] = 
	{
		Metadata.class, 
		Document.class, 
		ClippableDocument.class,
		Entity.class,
		Clipping.class,
		Image.class, 
		MediaClipping.class,
		ImageClipping.class,
		TextClipping.class,
		DebugMetadata.class,

		
	};

	public static TranslationScope get()
	{
		return TranslationScope.get(NAME, CLASSES);
	}	
}
