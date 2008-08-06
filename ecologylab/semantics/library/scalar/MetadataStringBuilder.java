package ecologylab.semantics.library.scalar;

import ecologylab.model.text.TermVector;
import ecologylab.semantics.metadata.semantics_pseudo_scalar;

@semantics_pseudo_scalar
public class MetadataStringBuilder extends MetadataScalarBase
{
	@xml_text StringBuilder value;
	
	public MetadataStringBuilder()
	{
		
	}
	public StringBuilder getValue()
	{
		return value;
	}
	
	public void setValue(StringBuilder value)
	{
		this.value = value;
	}
	
	public void setValue(String incomingValue)
	{
		value = (value == null) ? new StringBuilder(incomingValue) : value.append(incomingValue);
	}

	@Override
	public void contributeToTermVector(TermVector compositeTermVector)
	{
		if(value != null && value.length() > 0 )
		{
			compositeTermVector.addTerms(value.toString(), false);
		}	
	}
}
