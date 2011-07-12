/**
 * 
 */
package ecologylab.semantics.metametadata.test.deserialization;

import java.io.File;

import org.w3c.tidy.Tidy;

import ecologylab.generic.Debug;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;

/**
 * @author andruid
 *
 */
public class ClippingDeserializationTest extends Debug
{

	private static final TranslationScope	META_METADATA_TRANSLATIONS	= GeneratedMetadataTranslationScope.get();

	/**
	 * 
	 */
	public ClippingDeserializationTest()
	{
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] s)
	{
		File file	= new File("S:/cFprofiling/deserializationTest/textClippingDeserializationTest.xml");
		
		SemanticsSessionScope sss	= new SemanticsSessionScope(META_METADATA_TRANSLATIONS, Tidy.class);
		
		try
		{
			ElementState es	= META_METADATA_TRANSLATIONS.deserialize(file);

			es.serialize(System.out);
		}
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
