package ecologylab.bigsemantics.metametadata;

import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_tag;

@simpl_inherit
@simpl_tag("collection")
public class MetaMetadataCollectionFieldWithoutChildComposite extends MetaMetadataCollectionField
{

	@Override
	public void deserializationPostHook(TranslationContext translationContext, Object object)
	{
		// do nothing because we use this only for translating the repository.
	}
	
}
