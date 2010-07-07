/**
 * 
 */
package ecologylab.semantics.metadata;

import ecologylab.generic.Debug;
import ecologylab.semantics.metadata.builtins.DebugMetadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Entity;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.builtins.Media;
import ecologylab.xml.TranslationScope;

/**
 * 
 * 
 * @author bharat
 * 
 */
public class MetadataBuiltinsTranslationScope extends Debug
{
	public static final String NAME = "metadata_builtin_translations";
	
	protected static final Class CLASSES[] = 
	{
		Metadata.class, 
		Document.class, 
		Entity.class,
		Media.class, 
		Image.class, 
		DebugMetadata.class,
	};

	public static TranslationScope get()
	{
		return TranslationScope.get(NAME, CLASSES);
	}	
}
