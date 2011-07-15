package ecologylab.semantics.metadata.scalar.types;

import ecologylab.semantics.metadata.builtins.ImageClipping;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.ScalarUnmarshallingContext;

public class MetadataStringScalarType extends MetadataScalarType<MetadataString, String>
{

	public MetadataStringScalarType()
	{
		super(MetadataString.class, String.class, null, null);
	}

	/**
	 * Used in deserialization. Creates new instance. 
	 */
	@Override
	public MetadataString getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return new MetadataString(getValueInstance(value, formatStrings, scalarUnmarshallingContext));
	}

	public static void main(String[] args)
	{
		MetadataScalarType.init();
		
		ImageClipping i	= new ImageClipping();
		i.setCaption("a nice caption.");
		i.setContext("A much, much longer context");
		
		try
		{
			i.serialize(System.out);
		}
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
