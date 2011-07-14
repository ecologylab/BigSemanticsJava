/**
 * 
 */
package ecologylab.semantics.metametadata.test.deserialization;

import java.io.File;

import org.w3c.tidy.Tidy;

import ecologylab.generic.Debug;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.namesandnums.SemanticsNames;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;

/**
 * @author andruid
 *
 */
public class ClippingDeserializationTest extends Debug
implements SemanticsNames
{
	static
	{
		MetaMetadataRepository.initializeTypes();
	}
	private static final TranslationScope	META_METADATA_TRANSLATIONS	= RepositoryMetadataTranslationScope.get();

	private static final TranslationScope MY_TRANSLATIONS	= TranslationScope.get("mine",
			META_METADATA_TRANSLATIONS, InformationCompositionTest.class);
	/**
	 * 
	 */
	public ClippingDeserializationTest()
	{
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] s)
	{
		File file2	= new File("ecologylab/semantics/metametadata/test/deserialization/imageClipping.xml");

		File file		= new File("ecologylab/semantics/metametadata/test/deserialization/textClipping.xml");
		
		SemanticsSessionScope sss	= new SemanticsSessionScope(META_METADATA_TRANSLATIONS, Tidy.class);
		
		try
		{
//			ElementState es	= MY_TRANSLATIONS.deserialize(file);
//
//			es.serialize(System.out);
			
			ElementState es2	= MY_TRANSLATIONS.deserialize(file2);

			es2.serialize(System.out);
		}
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
