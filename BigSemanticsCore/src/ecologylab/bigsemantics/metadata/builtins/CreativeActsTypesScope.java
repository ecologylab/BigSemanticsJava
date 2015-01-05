/**
 * 
 */
package ecologylab.bigsemantics.metadata.builtins;

import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.builtins.declarations.MetadataBuiltinDeclarationsTranslationScope;
import ecologylab.bigsemantics.metadata.scalar.types.MetadataScalarType;
import ecologylab.generic.Debug;
import ecologylab.serialization.SimplTypesScope;

/**
 * Encapsulates ClassDescriptors for Metadata and its subclasses that are coded by hand.
 */
public class CreativeActsTypesScope extends Debug
{
	public static final String NAME = "creative_acts_scope";
	
	protected static final Class CLASSES[] = 
	{
		CreativeAct.class,
		CurateLink.class,
		AssignPrimaryLink.class,
		Annotate.class,
	};
	
	static
	{
		MetadataScalarType.init();
	}

	public static SimplTypesScope get()
	{
		return SimplTypesScope.get(NAME, CLASSES);
	}		
}
