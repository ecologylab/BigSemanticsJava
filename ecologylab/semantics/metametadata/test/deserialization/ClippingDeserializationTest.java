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
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.Format;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.StringFormat;
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
			
			ElementState es2	= (ElementState) MY_TRANSLATIONS.deserialize(file2, Format.XML);

			ClassDescriptor.serialize(es2, System.out, StringFormat.XML);

		}
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
