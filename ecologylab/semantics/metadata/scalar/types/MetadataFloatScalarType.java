package ecologylab.semantics.metadata.scalar.types;

import ecologylab.semantics.metadata.scalar.MetadataFloat;
import ecologylab.serialization.ScalarUnmarshallingContext;

public class MetadataFloatScalarType extends MetadataScalarScalarType<MetadataFloat, Float>
{

	public MetadataFloatScalarType()
	{
		super(MetadataFloat.class, Float.class);
	}

	@Override
	public MetadataFloat getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return new MetadataFloat(getValueInstance(value, formatStrings, scalarUnmarshallingContext));
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
	
	@Override
	public String getJavaType()
	{
		return MetadataFloat.class.getSimpleName();
	}
}
