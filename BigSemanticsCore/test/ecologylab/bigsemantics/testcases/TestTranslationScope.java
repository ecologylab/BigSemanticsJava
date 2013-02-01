package ecologylab.bigsemantics.testcases;


import ecologylab.bigsemantics.metadata.builtins.Image;
import ecologylab.bigsemantics.metadata.scalar.MetadataString;
import ecologylab.generic.Debug;
import ecologylab.serialization.SimplTypesScope;

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
		
	public static SimplTypesScope get()
	{
		return SimplTypesScope.get(PACKAGE_NAME, TRANSLATIONS);
	}
}
