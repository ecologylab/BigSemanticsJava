package ecologylab.semantics.gui;

import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.semantics.metadata.MetadataFieldAccessor;

/**
 * Passed to the listener of the value edit in incontext Metadata.
 * @author bharat
 */
public class EditValueEvent
{
	MetadataFieldAccessor metadataFieldAccessor;
	MetadataBase metadata;
	
	public EditValueEvent(MetadataFieldAccessor metadataFieldAccessor, MetadataBase metadata)
	{
		this.metadataFieldAccessor  = metadataFieldAccessor;
		this.metadata 				= metadata;
	}
	/**
	 * @return the metadataFieldAccessor
	 */
	public MetadataFieldAccessor getMetadataFieldAccessor()
	{
		return metadataFieldAccessor;
	}
	/**
	 * @param metadataFieldAccessor the metadataFieldAccessor to set
	 */
	public void setMetadataFieldAccessor(MetadataFieldAccessor metadataFieldAccessor)
	{
		this.metadataFieldAccessor = metadataFieldAccessor;
	}
	/**
	 * @return the metadata
	 */
	public MetadataBase getMetadata()
	{
		return metadata;
	}
	/**
	 * @param metadata the metadata to set
	 */
	public void setMetadata(MetadataBase metadata)
	{
		this.metadata = metadata;
	}
}
