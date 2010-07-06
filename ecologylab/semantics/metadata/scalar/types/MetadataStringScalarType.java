package ecologylab.semantics.metadata.scalar.types;

import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.xml.ScalarUnmarshallingContext;
import ecologylab.xml.XMLTranslationException;

public class MetadataStringScalarType extends MetadataScalarScalarType<MetadataString, String>
{

	public MetadataStringScalarType()
	{
		super(MetadataString.class, String.class);
	}

	@Override
	public MetadataString getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return new MetadataString(getValueInstance(value, formatStrings, scalarUnmarshallingContext));
	}

	public static void main(String[] args)
	{
		MetadataScalarScalarType.init();
		
		Image i	= new Image();
		i.setCaption("a nice caption.");
		i.setContext("A much, much longer context");
		
		try
		{
			i.translateToXML(System.out);
		}
		catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getCSharptType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDbType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getObjectiveCType() {
		// TODO Auto-generated method stub
		return null;
	}
}
