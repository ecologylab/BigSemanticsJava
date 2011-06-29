/**
 * 
 */
package ecologylab.semantics.metadata;

import java.util.ArrayList;

import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.simpl_inherit;

/**
 * Special class descriptor for Metadata subclasses.
 * 
 * @author andruid
 * 
 */
@simpl_inherit
public class MetadataClassDescriptor extends ClassDescriptor<Metadata, MetadataFieldDescriptor>
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

	public MetadataClassDescriptor(
			String tagName,
			String comment,
			String describedClassPackageName,
			String describedClassSimpleName,
			ClassDescriptor superClass,
			ArrayList<String> interfaces)
	{
		super(tagName, comment, describedClassPackageName, describedClassSimpleName, superClass, interfaces);
		// TODO Auto-generated constructor stub
	}
	
	public void addMetadataFieldDescriptor(MetadataFieldDescriptor fd)
	{
		this.addFieldDescriptor(fd);
	}
	
}
