package ecologylab.semantics.library.scalar;

import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.semantics.metadata.semantics_pseudo_scalar;


@semantics_pseudo_scalar
public class MetadataInteger extends MetadataScalarBase
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
	
//	public void setValue(int value)
//	{
//		this.value = new Integer(value);
//	}
	
}
