/**
 * 
 */
package ecologylab.semantics.metametadata.test;

import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;

/**
 * @author andruid
 *
 */
public class MetadataSerializationTest extends SemanticsTest
{
	/**
	 * 
	 */
	public MetadataSerializationTest() throws SIMPLTranslationException
	{
		super();
	}
	

	public static final String TEST1 = "<image mm_name=\"image\" caption=\"Summer field in Belgium (Hamois). The blue flower is Centaurea cyanus and the red one a Papaver rhoeas.\" location=\"http://upload.wikimedia.org/wikipedia/commons/thumb/c/c4/Field_Hamois_Belgium_Luc_Viatour.jpg/250px-Field_Hamois_Belgium_Luc_Viatour.jpg\"></image>";

  public static void main(String[] args)
  { 	
		try
		{
	  	new MetadataSerializationTest();

	  	final TranslationScope T_SCOPE	= GeneratedMetadataTranslationScope.get();

	  	ElementState image	= T_SCOPE.deserializeCharSequence(TEST1);
			
			image.serialize(System.out);
		}
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }
}
