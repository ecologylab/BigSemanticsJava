/**
 * 
 */
package ecologylab.semantics.metadata;

import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.ClassDescriptor;
import ecologylab.xml.xml_inherit;

/**
 * @author andruid
 *
 */
@xml_inherit
public class MetadataClassDescriptor<M extends Metadata> extends ClassDescriptor<M, MetadataFieldDescriptor>
{
	private MetaMetadata	metaMetadata;
	
	public MetadataClassDescriptor()
	{
		
	}
	public MetadataClassDescriptor(Class thatClass)
	{
		super(thatClass);
	}
	/**
	 * @return the metaMetadata
	 */
	public MetaMetadata getMetaMetadata()
	{
		return metaMetadata;
	}
	/**
	 * @param metaMetadata the metaMetadata to set
	 */
	public void setMetaMetadata(MetaMetadata metaMetadata)
	{
		this.metaMetadata = metaMetadata;
	}
}
