package testcases;

import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_hints;
import ecologylab.serialization.annotations.simpl_scalar;

public class TestDocument extends ElementState
{
	@simpl_scalar	String 			context;
	@simpl_composite 	MetadataString	title;
	@simpl_scalar @simpl_hints(Hint.XML_TEXT)		String			aValue;
	
	MetadataString title()
	{
		MetadataString result = this.title;
		if(result == null)
		{
			result = new MetadataString();
			this.title = result;
		}
		return result;
	}
	
	/**
	 * @return the context
	 */
	public String getContext()
	{
		return context;
	}
	/**
	 * @param context the context to set
	 */
	public void setContext(String context)
	{
		this.context = context;
	}
	/**
	 * @return the title
	 */
	public MetadataString getTitle()
	{
		return title();
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(MetadataString title)
	{
		this.title = title;
	}
	/**
	 * @return the value
	 */
	public String getValue()
	{
		return aValue;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value)
	{
		this.aValue = value;
	}
}
