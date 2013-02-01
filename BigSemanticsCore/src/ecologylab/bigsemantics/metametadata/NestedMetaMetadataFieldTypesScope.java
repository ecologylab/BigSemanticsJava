package ecologylab.bigsemantics.metametadata;

import ecologylab.generic.Debug;
import ecologylab.serialization.SimplTypesScope;

public class NestedMetaMetadataFieldTypesScope extends Debug
{
	
	public static final String		NAME						= "nested_meta_metadata_field_tscope";

	@SuppressWarnings("rawtypes")
	protected static final Class	TRANSLATIONS[]	= {
		MetaMetadataField.class,
		MetaMetadataScalarField.class,
		MetaMetadataCompositeField.class,
		MetaMetadataCollectionField.class,
	};

	public static SimplTypesScope get()
	{
		return SimplTypesScope.get(NAME, TRANSLATIONS);
	}
	
}
