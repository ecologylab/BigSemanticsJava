package ecologylab.semantics.metadata.scalar;

import ecologylab.serialization.TranslationScope;

public class MetadataScalarTranslationScope
{

	public static final String NAME = "metadata_scalar_translation_scope";
	
	protected static final Class[] TRANSLATIONS = {
		MetadataDate.class,
		MetadataFile.class,
		MetadataInteger.class,
		MetadataParsedURL.class,
		MetadataString.class,
		MetadataStringBuilder.class,
	};
	
	public static TranslationScope get()
	{
		return TranslationScope.get(NAME, TRANSLATIONS);
	}
	
}
