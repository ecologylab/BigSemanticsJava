package ecologylab.semantics.metametadata;

import ecologylab.generic.Debug;
import ecologylab.serialization.TranslationScope;

public class NestedMetaMetadataFieldTranslationScope extends Debug
{
	
	public static final String		NAME						= "nested_meta_metadata_field_tscope";

	@SuppressWarnings("rawtypes")
	protected static final Class	TRANSLATIONS[]	= {
		MetaMetadataField.class,
		MetaMetadataScalarField.class,
		MetaMetadataCompositeField.class,
		MetaMetadataCollectionField.class,
	};

	public static TranslationScope get()
	{
		return TranslationScope.get(NAME, TRANSLATIONS);
	}
	
}
