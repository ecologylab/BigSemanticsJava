/**
 * 
 */
package ecologylab.semantics.metadata;

import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.ClassDescriptor;
import ecologylab.xml.xml_inherit;

/**
 * Special class descriptor for Metadata subclasses.
 * 
 * @author andruid
 *
 */
@xml_inherit
public class MetadataClassDescriptor /* <M extends Metadata>*/ extends ClassDescriptor<Metadata, MetadataFieldDescriptor>
{
	public MetadataClassDescriptor()
	{
		
	}
	/**
	 * Called by reflection with a Metadata subclass.
	 * 
	 * @param thatClass
	 */
	public MetadataClassDescriptor(Class thatClass)
	{
		super(thatClass);
	}
}
