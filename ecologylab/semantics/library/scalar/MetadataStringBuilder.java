package ecologylab.semantics.library.scalar;

import ecologylab.generic.FeatureVector;
import ecologylab.semantics.metadata.semantics_pseudo_scalar;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.Term;
import ecologylab.semantics.model.text.TermVector;

@semantics_pseudo_scalar
public class MetadataStringBuilder extends MetadataScalarBase
{
	TermVector termVector = null;
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
		if (termVector != null)
			termVector.reset(value.toString());
		else
			termVector = new TermVector(value.toString());
	}
	
	public ITermVector termVector()
	{
		if (termVector == null)
			termVector = new TermVector();
		return termVector;
	}
	
	@Override
	public String toString()
	{
		return value == null ? "" : value.toString();
	}
}
