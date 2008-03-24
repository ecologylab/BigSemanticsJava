package ecologylab.semantics.metametadata;


import ecologylab.generic.Debug;
import ecologylab.xml.TranslationScope;

public class MetaMetadataTranslationSpace extends Debug
{
	public static final String NAME = "metaMetadata";
	public static final String PACKAGE_NAME = "metaMetadata";
	
	protected static final Class TRANSLATIONS[] = 
	{
		MetaMetadata.class,
		MetaMetadataField.class,
		MetaMetadataRepository.class,
	};
		
	public static TranslationScope get()
	{
		return TranslationScope.get(PACKAGE_NAME, TRANSLATIONS);
	}
}
