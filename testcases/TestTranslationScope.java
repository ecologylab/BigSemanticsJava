package testcases;


import ecologylab.generic.Debug;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.xml.TranslationScope;

public class TestTranslationScope extends Debug
{
	public static final String NAME = "metaMetadata";
	public static final String PACKAGE_NAME = "metaMetadata";
	
	protected static final Class TRANSLATIONS[] = 
	{
		TestDocument.class,
		MetadataString.class,
	};
		
	public static TranslationScope get()
	{
		return TranslationScope.get(PACKAGE_NAME, TRANSLATIONS);
	}
}
