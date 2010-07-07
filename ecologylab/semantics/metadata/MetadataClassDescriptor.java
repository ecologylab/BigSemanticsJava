/**
 * 
 */
package ecologylab.semantics.metadata;

import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.simpl_inherit;

/**
 * Special class descriptor for Metadata subclasses.
 * 
 * @author andruid
 *
 */
@simpl_inherit
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
