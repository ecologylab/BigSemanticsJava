package ecologylab.semantics.metametadata.test;

import ecologylab.semantics.metametadata.MetaMetadataCollectionField;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
@xml_tag("collection")
public class MetaMetadataCollectionFieldWithoutChildComposite extends MetaMetadataCollectionField
{

	@Override
	public void deserializationPostHook()
	{
		// do nothing because we use this only for translating the repository.
	}
	
}
