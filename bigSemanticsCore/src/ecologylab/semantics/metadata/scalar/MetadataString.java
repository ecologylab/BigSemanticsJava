/**
 * 
 */
package ecologylab.semantics.metadata.scalar;

import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.metadata.semantics_pseudo_scalar;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.TermVector;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * @author andruid
 *
 */
@simpl_inherit
@semantics_pseudo_scalar
public class MetadataString extends MetadataScalarBase<String>
{
	TermVector termVector = null;
	
	public MetadataString()
	{
	}

	public MetadataString(String value)
	{
		super(value);
	}
	/**
	 * Initialize the TermVector in addition to setting the value.
	 */
	@Override
	public void setValue(String value)
	{
		if ((value != null) && (value.length() > 0))
		{
			this.value = value;
			if (termVector != null)
			{
				termVector.reset(value);
			}
			else
			{
				termVector = new TermVector(value);
			}
		}
		else
		{
			this.value	= value;	// set to "" or null
			if (termVector != null)
			{
				termVector.reset("");
			}
		}

	}
	@Override
	public ITermVector termVector()
	{
		if (termVector == null)
			termVector = new TermVector(value); //Value may be non-null at this point. 
		return termVector;
	}
	
	/**
	 * Check if a string is not null and not equal to {@code MetadataFieldAccessor.NULL}
	 * @param valueString - string to check
	 * @return True if not null and not equal to MetadataFieldAccessor.NULL, false otherwise.
	 */
	public static boolean isNotNullValue(String valueString)
	{
		return (valueString != null && !valueString.equals(MetadataFieldDescriptor.NULL) );
	}
	
	public static boolean isNotNullAndEmptyValue(String valueString)
	{
		return isNotNullValue(valueString) && !"".equals(valueString.trim());
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
	
	/**
	 * String scalar fields, by type, should contribute to the CompositeTermVector.
	 * This can, of course, be overridden in a meta_metadata field description.
	 */
	@Override
	public boolean ignoreInTermVector()
	{
		return false;
	}

}
