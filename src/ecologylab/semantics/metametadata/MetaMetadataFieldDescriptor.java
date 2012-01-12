/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.lang.reflect.Field;

import ecologylab.semantics.metadata.mm_dont_inherit;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;

/**
 * @author andrew
 *
 */

public class MetaMetadataFieldDescriptor extends FieldDescriptor
{
	/**
	 * Should this field be inherited in meta-metadata
	 */
	final private boolean		isInheritable;
	
	public MetaMetadataFieldDescriptor(ClassDescriptor declaringClassDescriptor, Field field, int annotationType) // String nameSpacePrefix
	{
		super(declaringClassDescriptor, field, annotationType);
		if (field != null)
		{
			isInheritable				= !field.isAnnotationPresent(mm_dont_inherit.class);
		}
		else
		{
			isInheritable				= true;
		}
	}
	
	public MetaMetadataFieldDescriptor(ClassDescriptor baseClassDescriptor, FieldDescriptor wrappedFD, String wrapperTag)
	{
		super(baseClassDescriptor, wrappedFD, wrapperTag);
		isInheritable = true;
	}
	
	public boolean isInheritable()
	{
		return isInheritable;
	}
}
