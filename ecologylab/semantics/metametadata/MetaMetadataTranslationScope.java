package ecologylab.semantics.metametadata;


import ecologylab.generic.Debug;
import ecologylab.model.NamedStyle;
import ecologylab.net.UserAgent;
import ecologylab.xml.TranslationScope;

public class MetaMetadataTranslationScope extends Debug
{
	public static final String NAME = "metaMetadata";
	public static final String PACKAGE_NAME = "metaMetadata";
	
	protected static final Class TRANSLATIONS[] = 
	{
		MetaMetadata.class,
		MetaMetadataField.class,
		MetaMetadataRepository.class,
		UserAgent.class,
		NamedStyle.class
	};
		
	public static TranslationScope get()
	{
		return TranslationScope.get(PACKAGE_NAME, TRANSLATIONS);
	}
}
