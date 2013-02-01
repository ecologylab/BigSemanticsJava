/**
 * 
 */
package ecologylab.bigsemantics.metadata;

import java.util.ArrayList;

import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MmdGenericTypeVar;
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
	
	@Override
	public boolean isGenericClass()
	{
		for (MmdGenericTypeVar mmdGenericTypeVar : this.definingMmd.getMetaMetadataGenericTypeVars())
		{
			// if name and bound specified, this should be a new definition of a generic type var.
			// currently we require that a bound is needed, although in fact this can be omitted in some cases.
			if (mmdGenericTypeVar.getName() != null && mmdGenericTypeVar.getExtendsAttribute() != null)
			{
				return true;
			}
		}
		return false;
	}
	
}
