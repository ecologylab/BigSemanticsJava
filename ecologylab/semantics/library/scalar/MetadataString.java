/**
 * 
 */
package ecologylab.semantics.library.scalar;

import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.xml.xml_inherit;

/**
 * @author andruid
 *
 */
@xml_inherit
public class MetadataString extends MetadataBase
{
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
	}

}
