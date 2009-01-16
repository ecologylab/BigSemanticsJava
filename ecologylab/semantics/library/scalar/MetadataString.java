/**
 * 
 */
package ecologylab.semantics.library.scalar;

import ecologylab.generic.FeatureVector;
import ecologylab.semantics.metadata.semantics_pseudo_scalar;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.Term;
import ecologylab.semantics.model.text.TermVector;
import ecologylab.xml.xml_inherit;

/**
 * @author andruid
 *
 */
@xml_inherit
@semantics_pseudo_scalar
public class MetadataString extends MetadataScalarBase
{
	TermVector termVector = null;
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
	  if (value == null)
	    value = "";
		this.value = value;
		if (termVector != null)
			termVector.reset(value);
		else
			termVector = new TermVector(value);
	}
	
	public ITermVector termVector()
	{
		if (termVector == null)
			termVector = new TermVector();
		return termVector;
	}
}
