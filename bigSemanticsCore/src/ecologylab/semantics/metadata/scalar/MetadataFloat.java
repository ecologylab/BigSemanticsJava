package ecologylab.semantics.metadata.scalar;

import ecologylab.semantics.metadata.semantics_pseudo_scalar;
import ecologylab.semantics.model.text.NullTermVector;

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
