/**
 * 
 */
package ecologylab.semantics.library.scalar;

import ecologylab.model.text.TermVector;
import ecologylab.semantics.metadata.semantics_pseudo_scalar;
import ecologylab.semantics.model.text.XTermVector;
import ecologylab.xml.xml_inherit;

/**
 * @author andruid
 *
 */
@xml_inherit
@semantics_pseudo_scalar
public class MetadataString extends MetadataScalarBase
{
	XTermVector termVector = null;
	@xml_text	String		value;
	
	
	public MetadataString()
	{
	}
	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
		if (termVector != null)
			termVector.reset(value);
		else
			termVector = new XTermVector(value);
	}
	
	public XTermVector termVector()
	{
		if (termVector == null)
			termVector = new XTermVector();
		return termVector;
	}

	@Override
	public void contributeToTermVector(TermVector compositeTermVector)
	{
		if(value != null && value != "null")
		{
			compositeTermVector.addTerms(value, false);
		}	
	}
}
