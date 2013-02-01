package ecologylab.bigsemantics.gui;

import ecologylab.bigsemantics.metadata.MetadataBase;
import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;

/**
 * Passed to the listener of the value edit in incontext Metadata.
 * @author bharat
 */
public class EditValueEvent
{
	MetadataFieldDescriptor metadataFieldAccessor;
	MetadataBase metadata;
	
	public EditValueEvent(MetadataFieldDescriptor metadataFieldAccessor, MetadataBase metadata)
	{
		this.metadataFieldAccessor  = metadataFieldAccessor;
		this.metadata 				= metadata;
	}
	/**
	 * @return the metadataFieldAccessor
	 */
	public MetadataFieldDescriptor getMetadataFieldAccessor()
	{
		return metadataFieldAccessor;
	}
	/**
	 * @param metadataFieldAccessor the metadataFieldAccessor to set
	 */
	public void setMetadataFieldAccessor(MetadataFieldDescriptor metadataFieldAccessor)
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
