package ecologylab.semantics.metadata.scalar;

import java.nio.ByteBuffer;

import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.NullTermVector;

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
