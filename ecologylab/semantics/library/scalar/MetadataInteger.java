package ecologylab.semantics.library.scalar;

import ecologylab.model.text.TermVector;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.xml.ElementState.xml_text;


public class MetadataInteger extends MetadataBase
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
	
	//FIXME:Integer Contributing to termVector??!!!
//	@Override
//	public void contributeToTermVector(TermVector compositeTermVector)
//	{
//		if(value != null && value != "null")
//		{
//			compositeTermVector.addTerms(value, false);
//		}	
//	}
}
