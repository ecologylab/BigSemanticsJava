package ecologylab.semantics.metadata.scalar;

import ecologylab.semantics.metadata.semantics_pseudo_scalar;
import ecologylab.semantics.model.text.NullTermVector;


@semantics_pseudo_scalar
public class MetadataInteger extends MetadataScalarBase<Integer>
{
	public MetadataInteger()
	{
	}
	
	public MetadataInteger(Integer value)
	{
		super(value);
	}
	
	@Override
	public NullTermVector termVector() 
	{
		return NullTermVector.singleton();
	}
	
}
