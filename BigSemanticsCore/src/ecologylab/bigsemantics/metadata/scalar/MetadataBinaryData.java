package ecologylab.bigsemantics.metadata.scalar;

import java.nio.ByteBuffer;

import ecologylab.bigsemantics.model.text.ITermVector;
import ecologylab.bigsemantics.model.text.NullTermVector;

public class MetadataBinaryData  extends MetadataScalarBase<ByteBuffer>
{
	public MetadataBinaryData()
	{
	}
	
	public MetadataBinaryData(ByteBuffer value)
	{
		super(value);
	}
	
	@Override
	public ITermVector termVector() {
		return NullTermVector.singleton();
	}

}
