package testcases;


import ecologylab.generic.Debug;
import ecologylab.semantics.library.Image;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.xml.TranslationScope;

public class TestTranslationScope extends Debug
{
	public static final String NAME = "testTranslationScope";
	public static final String PACKAGE_NAME = "testTranslationScope";
	
	protected static final Class TRANSLATIONS[] = 
	{
		TestDocument.class,
		MetadataString.class,
		Image.class
	};
		
	public static TranslationScope get()
	{
		return TranslationScope.get(PACKAGE_NAME, TRANSLATIONS);
	}
}
