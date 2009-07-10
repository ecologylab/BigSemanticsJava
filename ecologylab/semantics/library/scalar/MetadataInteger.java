package ecologylab.semantics.library.scalar;

import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.semantics.metadata.semantics_pseudo_scalar;
import ecologylab.semantics.model.text.NullTermVector;


@semantics_pseudo_scalar
public class MetadataInteger extends MetadataScalarBase<Integer>
{
	@xml_text	int		value;
	
	public MetadataInteger()
	{
	}
	public Integer getValue()
	{
		return value;
	}

	public void setValue(int value)
	{
		this.value = value;
	}
	
	@Override
	public NullTermVector termVector() 
	{
		return NullTermVector.singleton();
	}
	
//	public void setValue(int value)
//	{
//		this.value = new Integer(value);
//	}
	
}
