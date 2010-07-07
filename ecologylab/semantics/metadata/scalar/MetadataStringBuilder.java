package ecologylab.semantics.metadata.scalar;

import ecologylab.generic.StringTools;
import ecologylab.semantics.metadata.semantics_pseudo_scalar;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.TermVector;
import ecologylab.serialization.XMLTools;

@semantics_pseudo_scalar
public class MetadataStringBuilder extends MetadataScalarBase<StringBuilder>
{
	TermVector termVector = null;
	
	public MetadataStringBuilder()
	{
		
	}
	public MetadataStringBuilder(StringBuilder stringBuilder)
	{
		super(stringBuilder);
	}
	public void setValue(String incomingValue)
	{
		if ((incomingValue != null) && (incomingValue.length() > 0))
		{	
			boolean newValue	= false;
			if (value == null)
			{
				value = new StringBuilder(incomingValue);
				newValue				= true;
			}
			else
				value.append(incomingValue);
			
			XMLTools.unescapeXML(value);
			
			if (termVector != null)
				termVector.reset(value);
			else
			{
				termVector = new TermVector(newValue ? incomingValue : value);
			}
		}
		else
		{
			//FIXME -- should this be done or is it really meant to be an append in this case
			//TODO -- how are we differentiating between accumulating data and editing?
			if (value != null)
				StringTools.clear(value);
			else
				value		= null;
			if (termVector != null)
				termVector.reset("");
		}
	}
	@Override
	public ITermVector termVector()
	{
		if (termVector == null)
			termVector = new TermVector();
		return termVector;
	}
	@Override
	public void recycle()
	{
		if (termVector != null)
		{
			termVector.recycle();
			termVector	= null;
		}
	}

	@Override
	public String toString()
	{
		return value == null ? "" : value.toString();
	}
}
