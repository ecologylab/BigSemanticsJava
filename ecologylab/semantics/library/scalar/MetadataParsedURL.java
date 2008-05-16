/**
 * 
 */
package ecologylab.semantics.library.scalar;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_text;

/**
 * @author bharat
 *
 */
@xml_inherit
public class MetadataParsedURL extends Metadata
{
	@xml_text	ParsedURL		value;
	
	public MetadataParsedURL()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param metaMetadata
	 */
	public MetadataParsedURL(MetaMetadata metaMetadata)
	{
		super(metaMetadata);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the value
	 */
	public ParsedURL getValue()
	{
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(ParsedURL value)
	{
		this.value = value;
	}
}
