/**
 * 
 */
package ecologylab.semantics.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import ecologylab.generic.ReflectionTools;
import ecologylab.semantics.gui.EditValueEvent;
import ecologylab.semantics.gui.EditValueListener;
import ecologylab.semantics.gui.EditValueNotifier;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.xml.ClassDescriptor;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldDescriptor;
import ecologylab.xml.ScalarUnmarshallingContext;
import ecologylab.xml.types.scalar.ScalarType;

/**
 * @author andruid
 *
 */
public class MetadataFieldDescriptor<M extends Metadata> extends FieldDescriptor implements EditValueNotifier
{
	final private boolean		isPseudoScalar;
	
	final private boolean		isMixin;
	
	Method									hwSetMethod;
	
	MetaMetadataField				metaMetadataField;

	private ArrayList<EditValueListener> editValueListeners = new ArrayList<EditValueListener>();
	
	public MetadataFieldDescriptor(ClassDescriptor declaringClassDescriptor, Field field, int annotationType) // String nameSpacePrefix
	{
		super(declaringClassDescriptor, field, annotationType);
		if (field != null)
		{
			isMixin							= field.isAnnotationPresent(Metadata.semantics_mixin.class);

			Class<?> thatClass	= field.getType();
			isPseudoScalar	= thatClass.isAnnotationPresent(semantics_pseudo_scalar.class);
			
			//TODO -- for future expansion??? andruid 4/14/09
//			hwSetMethod					= ReflectionTools.getMethod(thatClass, "hwSet", SET_METHOD_ARG);
		}
		else
		{
			isMixin							= false;
			isPseudoScalar			= false;		
		}
	}
	
	public MetadataFieldDescriptor(ClassDescriptor baseClassDescriptor, FieldDescriptor wrappedFD, String wrapperTag)
	{
		super(baseClassDescriptor, wrappedFD, wrapperTag);
		isMixin	= false;
		isPseudoScalar	= false;
	}
	
	public boolean isNonNullReference(MetadataBase context)
	{
		return (getScalarType() == null) && super.isNonNullReference((ElementState) context);
	}
	public boolean isPseudoScalar() 
	{
		return isPseudoScalar;
	}

	public boolean isMixin() 
	{
		return isMixin;
	}

	public void addEditValueListener(EditValueListener listener)
	{
		editValueListeners.add(listener);
	}

/**
 * Edit the value of a scalar.
 */
	public void fireEditValue(Metadata metadata, String fieldValueString)
	{
		if (isScalar())
		{
			if(this.set(metadata, fieldValueString))	// uses reflection to call a set method or access the field directly if there is not one.
			{
				metadata.rebuildCompositeTermVector();	// makes this as if an hwSet().
				
				//Call the listeners only after the field is properly set.
				EditValueEvent event = new EditValueEvent(this, metadata);
	
				for(EditValueListener listener : editValueListeners)
				{
					listener.editValue(event);
				}
			}
		}
	}
	
	public void removeEditValueListener(EditValueListener listener)
	{
		editValueListeners.remove(listener);
	}
	
	public ElementState getNested(MetadataBase context)
	{
		return isScalar() ? null : getNested((ElementState) context);
	}

	/**
	 * @return the metaMetadataField
	 */
	public MetaMetadataField getMetaMetadataField()
	{
		return metaMetadataField;
	}

	/**
	 * @param metaMetadataField the metaMetadataField to set
	 */
	public void setMetaMetadataField(MetaMetadataField metaMetadataField)
	{
		this.metaMetadataField = metaMetadataField;
	}
	
	@Override
	protected void setFieldToScalar(Object context, String value, ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		
		super.setFieldToScalar(context, value, scalarUnmarshallingContext);
	}
}
