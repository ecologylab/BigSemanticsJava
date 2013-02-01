package ecologylab.bigsemantics.metadata.scalar.types;

import ecologylab.bigsemantics.metadata.scalar.MetadataFloat;
import ecologylab.serialization.ScalarUnmarshallingContext;

public class MetadataFloatScalarType extends MetadataScalarType<MetadataFloat, Float>
{

	public MetadataFloatScalarType()
	{
		super(MetadataFloat.class, Float.class, null, null);
	}

	@Override
	public MetadataFloat getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return new MetadataFloat(getValueInstance(value, formatStrings, scalarUnmarshallingContext));
	}

}
