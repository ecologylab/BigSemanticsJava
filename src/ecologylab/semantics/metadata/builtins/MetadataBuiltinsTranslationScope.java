/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.generic.Debug;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.declarations.MetadataBuiltinDeclarationsTranslationScope;
import ecologylab.semantics.metadata.scalar.types.MetadataScalarType;
import ecologylab.serialization.SimplTypesScope;

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
		CompoundDocument.class,
		ClippableDocument.class,
		Clipping.class,
		Image.class, 
		MediaClipping.class,
		ImageClipping.class,
		TextClipping.class,
		DebugMetadata.class,
		DocumentMetadataWrap.class,
		Annotation.class,
	};
	
	static
	{
		MetadataScalarType.init();
	}

	public static SimplTypesScope get()
	{
		return SimplTypesScope.get(NAME, MetadataBuiltinDeclarationsTranslationScope.get(), CLASSES);
	}	
}
