/**
 * 
 */
package ecologylab.semantics.library.scalar;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.xml.xml_inherit;

/**
 * @author bharat
 *
 */
@xml_inherit
public class MetadataParsedURL extends MetadataBase
{
	@xml_text	ParsedURL		value;
	
	public MetadataParsedURL()
	{
	}

	public ParsedURL getValue()
	{
		return value;
	}

	public void setValue(ParsedURL value)
	{
		this.value = value;
	}
}
