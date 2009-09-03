package testcases;

import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.xml.ElementState;
import ecologylab.xml.ElementState.xml_nested;
import ecologylab.xml.ElementState.xml_text;

public class TestDocument extends ElementState
{
	@xml_attribute	String 			context;
	@xml_nested 	MetadataString	title;
	@xml_text		String			aValue;
	
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
