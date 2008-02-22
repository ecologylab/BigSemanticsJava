package ecologylab.semantics.metametadata;


import ecologylab.generic.Debug;
import ecologylab.xml.TranslationSpace;

public class MetaMetadataTranslationSpace extends Debug
{
	public static final String NAME = "metaMetadata";
	public static final String PACKAGE_NAME = "metaMetadata";
	
	protected static final Class TRANSLATIONS[] = 
	{
		MetaMetadata.class,
		MetaMetadataField.class
	};
		
	public static TranslationSpace get()
	{
		return TranslationSpace.get(PACKAGE_NAME, TRANSLATIONS);
	}
}
