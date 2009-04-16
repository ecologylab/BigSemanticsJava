/**
 * 
 */
package ecologylab.semantics.library.scalar;

import com.sun.corba.se.impl.oa.toa.TOA;

import ecologylab.generic.FeatureVector;
import ecologylab.semantics.metadata.MetadataFieldAccessor;
import ecologylab.semantics.metadata.semantics_pseudo_scalar;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.Term;
import ecologylab.semantics.model.text.TermVector;
import ecologylab.xml.XMLTools;
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
//	  if (value == null)
//	    value = "";
		if ((value != null) && (value.length() > 0))
		{
		  	value		= XMLTools.unescapeXML(value);
			this.value = value;
			if (termVector != null)
				termVector.reset(value);
			else
				termVector = new TermVector(value);
		}
		else
		{
			this.value	= value;	// set to "" or null
			if (termVector != null)
				termVector.reset("");
		}

	}
	
	public ITermVector termVector()
	{
		if (termVector == null)
			termVector = new TermVector();
		return termVector;
	}
	
	/**
	 * Check if a string is not null and not equal to {@code MetadataFieldAccessor.NULL}
	 * @param valueString - string to check
	 * @return True if not null and not equal to MetadataFieldAccessor.NULL, false otherwise.
	 */
	public static boolean isNotNullValue(String valueString)
	{
		return (valueString != null && !valueString.equals(MetadataFieldAccessor.NULL) );
	}
}
