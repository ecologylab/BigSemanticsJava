/**
 * 
 */
package ecologylab.semantics.metametadata;

import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * @author andrew
 *
 */

@simpl_inherit
public class MetaMetadataClassDescriptor extends ClassDescriptor<MetaMetadataFieldDescriptor>
{
	public MetaMetadataClassDescriptor()
	{
		
	}
	
	public MetaMetadataClassDescriptor(Class thatClass)
	{
		super(thatClass);
	}
}
