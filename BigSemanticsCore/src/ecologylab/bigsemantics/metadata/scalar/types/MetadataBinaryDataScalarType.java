package ecologylab.bigsemantics.metadata.scalar.types;

import java.nio.ByteBuffer;

import ecologylab.bigsemantics.metadata.scalar.MetadataBinaryData;
import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.TranslationContext;

public class MetadataBinaryDataScalarType extends MetadataScalarType<MetadataBinaryData, ByteBuffer>
{
	public MetadataBinaryDataScalarType()
	{
		super(MetadataBinaryData.class, ByteBuffer.class, null, null);
	}
	
	@Override
	public MetadataBinaryData getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return new MetadataBinaryData(getValueInstance(value, formatStrings, scalarUnmarshallingContext));
	}
	
	@Override
	public String marshall(MetadataBinaryData instance, TranslationContext serializationContext)
	{
		return operativeScalarType().marshall(instance.getValue(), serializationContext);
	}
}
