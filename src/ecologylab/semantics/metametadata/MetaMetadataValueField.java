package ecologylab.semantics.metametadata;

import ecologylab.collections.Scope;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_hints;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;

/**
 * Represents a constant, scalar, or variable value which can be referenced in metametadata semantics.
 * @author twhite
 */
@simpl_tag("value")
public class MetaMetadataValueField
{
	/**
	 * The variable that data should be obtained from
	 */
	@simpl_scalar
	public String fromVar;
	/**
	 * The scalar value to get data from
	 */
	@simpl_scalar
	public String fromScalar;
	
	/**
	 * A constant value to use contained in the <value> tag </value>
	 */
	@simpl_scalar
	@simpl_hints(Hint.XML_TEXT)
	public String constantValue;
	
	
	public String getFromVar()
	{
		return this.fromVar;
	}
	
	public void setFromVar(String value)
	{
		this.fromVar = value;
	}
	
	public String getFromScalar()
	{
		return this.fromScalar;
	}
	
	public void setFromScalar(String value)
	{
		this.fromScalar = value;
	}
	
	public String getConstantValue()
	{
		return this.constantValue;
	}
	
	public void setConstantValue(String value)
	{
		this.constantValue = value;
	}	
	
	private String n(String s)
	{
		if(s == null)
		{
			return "";
		}else{
			return s;
		}
	}
	
	@Override
	public String toString()
	{
		return "from_var: \"" + n(this.fromVar) + "\" from_scalar: \""+ n(this.fromScalar) + "\" Constant value: \"" + n(this.constantValue)+"\"";
	}
	
	public String getReferenceName()
	{
		if(this.fromVar != null)
		{
			return this.fromVar;
		}
		else if(this.fromScalar != null)
		{
			return this.fromScalar;
		}else{
			return "Constant Value";
		}
	}
	
	/**
	 * Obtains the string value referenced by this MetaMetadataValue in a given scope for a given metadata object
	 * @param mmdField
	 * @param metadata
	 * @param params
	 * @return
	 */
	public <MetaData> String getReferencedValue(MetaMetadataScalarField mmdField, Metadata metadata, Scope<Object> params)
	{
		String value = null;
		if(this.fromScalar != null)
		{
			MetadataFieldDescriptor<Metadata> mhd = metadata.getFieldDescriptorByTagName(this.fromScalar);
			if(mhd != null)
			{
				value = mhd.getValueString(metadata);
			}
		}
		else if(this.fromVar != null)
		{
			Object varVal = params.get(this.fromVar);
			if(varVal != null)
			{
				value = (String)varVal;
			}
		}
		else if(this.constantValue != null)
		{
			value = this.getConstantValue();
		}else{
			value = null;
		}
		return value;
	}
	
}
