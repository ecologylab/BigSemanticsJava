/**
 * 
 */
package ecologylab.semantics.metadata;

import java.util.ArrayList;

import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * Special class descriptor for Metadata subclasses.
 * 
 * @author andruid
 * 
 */
@simpl_inherit
public class MetadataClassDescriptor extends ClassDescriptor<MetadataFieldDescriptor> implements Cloneable
{
	
	private MetaMetadata definingMmd;
	
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
			MetaMetadata definingMmd,
			String tagName,
			String comment,
			String describedClassPackageName,
			String describedClassSimpleName,
			ClassDescriptor superClass,
			ArrayList<String> interfaces)
	{
		super(tagName, comment, describedClassPackageName, describedClassSimpleName, superClass, interfaces);
		this.definingMmd = definingMmd;
	}
	
	public void addMetadataFieldDescriptor(MetadataFieldDescriptor fd)
	{
		this.addFieldDescriptor(fd);
	}
	
	/**
	 * @return the definingMmd
	 */
	public MetaMetadata getDefiningMmd()
	{
		return definingMmd;
	}
	
	public void setDefiningMmd(MetaMetadata mmd)
	{
		this.definingMmd = mmd;
	}
	
}
