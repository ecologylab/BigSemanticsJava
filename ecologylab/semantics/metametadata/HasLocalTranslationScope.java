package ecologylab.semantics.metametadata;

import ecologylab.serialization.TranslationScope;

/**
 * This interface marks classes that can hold a local translation scope, e.g. a meta-metadata
 * package or a meta-metadata.
 * 
 * @author quyin
 *
 */
public interface HasLocalTranslationScope
{

	/**
	 * 
	 * @return The local translation scope.
	 */
	TranslationScope getLocalTranslationScope();
	
}
