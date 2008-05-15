/**
 * 
 */
package ecologylab.semantics.library.scalar;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.xml_inherit;

/**
 * @author andruid
 *
 */
@xml_inherit
public class MetadataString extends Metadata
{
	@xml_text	String		value;
	
	/**
	 *  
	 */
	public MetadataString()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param metaMetadata
	 */
	public MetadataString(MetaMetadata metaMetadata)
	{
		super(metaMetadata);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

}
