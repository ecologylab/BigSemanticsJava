package ecologylab.bigsemantics.metadata.scalar;

import ecologylab.bigsemantics.metadata.semantics_pseudo_scalar;
import ecologylab.bigsemantics.model.text.NullTermVector;

@semantics_pseudo_scalar
public class MetadataFloat extends MetadataScalarBase<Float>
{

	public MetadataFloat()
	{
	}
	
	public MetadataFloat(Float value)
	{
		super(value);
	}
	
	@Override
	public NullTermVector termVector() 
	{
		return NullTermVector.singleton();
	}

}
